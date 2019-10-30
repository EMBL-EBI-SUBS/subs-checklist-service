package uk.ac.ebi.subs.checklistservice.services.internal.archivechecklists;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class EnaChecklistService implements ArchiveChecklistService {

    public static final String EXECUTION_SUMMARY_FILE_NAME = "exec-summary.json";

    private static final String CHECKLIST_SUMMARY_URL = "https://www.ebi.ac.uk/ena/browser/api/summary/ERC000001-ERC999999";
    private static final String CHECKLIST_FETCH_URL = "https://www.ebi.ac.uk/ena/browser/api/xml/";

    private static final Logger LOGGER = LoggerFactory.getLogger(EnaChecklistService.class);

    private static final Map<String, String> CHECKLIST_NAME_LATEST_FILE_NAME_MAP = new HashMap<>(128);

    @Value("${checklist-service.archive.ena.checklist.localcopydir}")
    private String localCopyDir;

    @Value("${checklist-service.archive.ena.checklist.executionsummarydir}")
    private String execSummaryDir;

    @Value("${checklist-service.archive.ena.checklist.conversionscript.path}")
    private String conversionScriptPathStr;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private MappingMongoConverter mappingMongoConverter;

    @Autowired
    private ObjectMapper objectMapper;

    private String dateStamp;

    private ExecutionSummary execSummary;

    @Override
    public List<String> getUpdatedChecklists() {
        dateStamp = LocalDate.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"));

        execSummary.dateTime = new Date();

        List<String> res = getAvailableChecklistsNames().parallelStream()
                .map(checklistName -> {
                    storeUpdateLocally(checklistName);
                    return generateChecklist(checklistName);
                })
                .filter(Objects::nonNull)
                .map(this::updateChecklist)
                .collect(Collectors.toList());

        saveExecutionSummary();

        return res;
    }

    private List<String> getAvailableChecklistsNames() {
        LOGGER.debug("Getting available checklists names.");

        ObjectNode summary = restTemplate.getForObject(CHECKLIST_SUMMARY_URL, ObjectNode.class);

        List<String> result = new ArrayList<>(64);

        summary.withArray("summaries").forEach(accessionObject -> {
            String accession = accessionObject.get("accession").asText();

            result.add(accession);
        });

        return result;
    }

    private void storeUpdateLocally(String checklistName) {
        LOGGER.debug("Storing update locally for checklist : {}", checklistName);

        byte[] checklistXml = restTemplate.getForObject(CHECKLIST_FETCH_URL + checklistName, byte[].class);

        byte[] newChecklistChecksum = DigestUtils.md5(checklistXml);

        try {
            String existingFileName = CHECKLIST_NAME_LATEST_FILE_NAME_MAP.get(checklistName);
            if (existingFileName != null) {
                InputStream checklistIS = Files.newInputStream(Paths.get(localCopyDir, existingFileName));

                byte[] existingChecklistChecksum = DigestUtils.md5(checklistIS);

                checklistIS.close();

                if (Arrays.equals(newChecklistChecksum, existingChecklistChecksum)) {
                    LOGGER.debug("Local checklist copy is already up-to-date : {}", checklistName);

                    execSummary.checklist(checklistName).localCopyUpdated = Boolean.FALSE;

                    return;
                }
            }

            String newFileName = checklistName + "-" + dateStamp + ".xml";
            Files.write(Paths.get(localCopyDir, newFileName), checklistXml);

            LOGGER.info("Checklist updated : {}", newFileName);

            CHECKLIST_NAME_LATEST_FILE_NAME_MAP.put(checklistName, newFileName);

            execSummary.checklist(checklistName).localCopyUpdated = Boolean.TRUE;
            execSummary.checklist(checklistName).systemUptodate = Boolean.FALSE;
        } catch (IOException e) {
            throw new RuntimeException("Error storing update for checklist : " + checklistName, e);
        }
    }

    private Checklist generateChecklist(String checklistName) {
        LOGGER.debug("Generating checklist for : {}", checklistName);

        if (!execSummary.checklist(checklistName).localCopyUpdated
                && execSummary.checklist(checklistName).systemUptodate) {
            LOGGER.debug("No need to generate checklist : {}. Already up-to-date.", checklistName);

            return null;
        }

        try {
            String genResult = runScript(checklistName);

            LOGGER.debug("Reading converter results into checklist object for : {}", checklistName);

            DBObject dbObject = (DBObject)JSON.parse(genResult);

            Checklist genChecklist = mappingMongoConverter.read(Checklist.class, dbObject);

            LOGGER.debug("Checklist object created for : {}", checklistName);

            return genChecklist;
        } catch (Exception e) {
            LOGGER.error("Error generating checklist for : " + checklistName, e);

            return null;
        }
    }

    private String updateChecklist(Checklist checklist) {
        LOGGER.debug("Updating checklist in the system : {}", checklist.getId());

        checklistRepository.save(checklist);

        LOGGER.debug("Checklist updated in the system : {}", checklist.getId());

        execSummary.checklist(checklist.getId()).systemUptodate = Boolean.TRUE;

        return checklist.getId();
    }

    @PostConstruct
    private void setup() {
        try {
            Files.createDirectories(Paths.get(localCopyDir));

            Files.createDirectories(Paths.get(execSummaryDir));

            loadExecutionSummary();

            Files.list(Paths.get(localCopyDir)).forEach(filePath -> {
                String fileName = filePath.toFile().getName();
                String checklistName = fileName.split("-")[0];

                String mapFileName = CHECKLIST_NAME_LATEST_FILE_NAME_MAP.get(checklistName);
                if (mapFileName == null || fileName.compareTo(mapFileName) == 1) {
                    CHECKLIST_NAME_LATEST_FILE_NAME_MAP.put(checklistName, fileName);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadExecutionSummary() {
        LOGGER.debug("Loading execution summary.");

        Path execSummaryPath = Paths.get(execSummaryDir, EXECUTION_SUMMARY_FILE_NAME);

        if (!Files.exists(execSummaryPath)) {
            LOGGER.debug("Previous execution summary not found : {}", execSummaryPath);

            execSummary = new ExecutionSummary();
            return;
        }

        try {
            execSummary = objectMapper.readValue(Files.newInputStream(execSummaryPath), ExecutionSummary.class);

            LOGGER.debug("Execution summary loaded : {}", execSummaryPath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading execution summary : " + execSummaryPath.toString(), e);
        }
    }

    private void saveExecutionSummary() {
        LOGGER.debug("Saving execution summary.");

        Path execSummaryPath = Paths.get(execSummaryDir, EXECUTION_SUMMARY_FILE_NAME);

        try {
            Files.write(execSummaryPath, objectMapper.writeValueAsBytes(execSummary));

            LOGGER.debug("Execution summary saved : {}", execSummaryPath.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error writing execution summary : " + execSummaryPath.toString(), e);
        }
    }

    private String runScript(String checklistName) {
        LOGGER.debug("Running checklist conversion script for : {}", checklistName);

        ProcessBuilder processBuilder = new ProcessBuilder(conversionScriptPathStr, checklistName);
        Process process = null;
        String stdOut = null, stdErr = null;
        try {
            process = processBuilder.start();

            stdOut = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
            stdErr = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);

            if (process.waitFor(30, TimeUnit.SECONDS) == false) {
                throw new RuntimeException("Conversion script took too long to complete.");
            }
            if (process.exitValue() != 0) {
                throw new RuntimeException("Conversion script exited with error.");
            }

            return stdOut;
        } catch (Exception e) {
            throw new RuntimeException("Error running conversion script for : " + checklistName +
                    ", StandardOut : " + stdOut + ", StandardError : " + stdErr, e);
        }
    }
}

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class ExecutionSummary {
    public Date dateTime;

    private Map<String, ChecklistStats> checklistStats = new HashMap<>();

    @JsonIgnore
    public ChecklistStats checklist(String checklistName) {
        ChecklistStats res = checklistStats.get(checklistName);
        if (res == null) {
            res = new ChecklistStats();
            checklistStats.put(checklistName, res);
        }

        return res;
    }
}

class ChecklistStats {
    public Boolean localCopyUpdated = Boolean.FALSE;

    public Boolean systemUptodate = Boolean.FALSE;
}
