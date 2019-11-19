package uk.ac.ebi.subs.checklistservice.services.internal.archivechecklists;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.checklistservice.services.internal.archive.checklist.ena.EnaChecklistService;
import uk.ac.ebi.subs.checklistservice.services.internal.archive.checklist.ena.UsiChecklistGeneratorService;
import uk.ac.ebi.subs.checklistservice.test.config.Config;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Config.class})
@TestPropertySource(locations="classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EnaChecklistServiceTest {

    @Value("${checklist-service.archive.ena.checklist.localcopydir}")
    private String localCopyDir;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private UsiChecklistGeneratorService usiChecklistGeneratorService;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private EnaChecklistService enaChecklistService;

    private List<String> expectedChecklistIds;

    @BeforeClass
    public static void beforeClass() throws IOException {
        clearChecklistDirectory();
    }

    @Before
    public void before() throws IllegalAccessException, IOException {
        mockExternalResources();

        checklistRepository.deleteAll();
    }

    @After
    public void after() throws IOException {
        clearChecklistDirectory();
    }

    @Test
    public void testSuccessful() throws IOException {
        Assert.assertNotNull(enaChecklistService);

        List<String> updatedChecklistIds = enaChecklistService.getUpdatedChecklists();

        List<Path> allFiles = Files.list(Paths.get(localCopyDir)).collect(Collectors.toList());

        updatedChecklistIds.forEach(checklistId -> {
            Assert.assertTrue(expectedChecklistIds.contains(checklistId));

            Assert.assertNotNull(checklistRepository.findOne(checklistId));

            Assert.assertTrue(allFiles.stream()
                    .anyMatch(filePath -> filePath.toFile().getName().contains(checklistId)));
        });
    }

    @Test
    public void testUpdateAfterFailure() throws IOException {
        Assert.assertNotNull(enaChecklistService);

        // choose a checklist id.
        String checklistId = expectedChecklistIds.get(0);
        Assert.assertNotNull(checklistId);

        // reconfigure mocking
        String beforeRemock = usiChecklistGeneratorService.generate(checklistId);
        Mockito.when(usiChecklistGeneratorService.generate(checklistId))
                .thenThrow(new RuntimeException("Something went wrong. Eh."))
                .thenReturn(beforeRemock);

        // make sure the err'ed checklist id is not present afterwards.
        Assert.assertFalse(enaChecklistService.getUpdatedChecklists().contains(checklistId));
        Assert.assertNull(checklistRepository.findOne(checklistId));

        // assert that file did get created even when the generation failed.
        Assert.assertTrue(Files.list(Paths.get(localCopyDir))
                .anyMatch(filePath -> filePath.toFile().getName().contains(checklistId)));

        // assert that checklist updated when generation succeeded on second attempt.
        Assert.assertTrue(enaChecklistService.getUpdatedChecklists().contains(checklistId));
        Assert.assertNotNull(checklistRepository.findOne(checklistId));
    }

    private void mockExternalResources() throws IOException, IllegalAccessException {
        Field summaryUrlField = ReflectionUtils.findField(EnaChecklistService.class, "CHECKLIST_SUMMARY_URL");
        summaryUrlField.setAccessible(true);
        String summaryUrl = (String) summaryUrlField.get(enaChecklistService);

        Field fetchUrlField = ReflectionUtils.findField(EnaChecklistService.class, "CHECKLIST_FETCH_URL");
        fetchUrlField.setAccessible(true);
        String fetchUrl = (String) fetchUrlField.get(enaChecklistService);

        ObjectMapper objectMapper = new ObjectMapper();

        Resource enaSummaryResource = new ClassPathResource("checklist/ena/summary.json");

        ObjectNode summaryObject = objectMapper.readValue(enaSummaryResource.getInputStream(), ObjectNode.class);

        Mockito.when(restTemplate.getForObject(summaryUrl, ObjectNode.class)).thenReturn(summaryObject);

        expectedChecklistIds = new ArrayList<>(128);

        for (JsonNode accessionObject : summaryObject.withArray("summaries")) {
            String accession = accessionObject.get("accession").asText().trim();

            Resource enaXmlChecklistResource = new ClassPathResource("checklist/ena/" + accession + ".xml");
            Mockito.when(restTemplate.getForObject(fetchUrl + accession, byte[].class))
                    .thenReturn(IOUtils.toByteArray(enaXmlChecklistResource.getInputStream()));

            Resource usiJsonChecklistResource = new ClassPathResource("checklist/usi/" + accession + ".json");
            Mockito.when(usiChecklistGeneratorService.generate(accession))
                    .thenReturn(IOUtils.toString(usiJsonChecklistResource.getInputStream(), StandardCharsets.UTF_8));

            expectedChecklistIds.add(accession);
        }
    }

    private static void clearChecklistDirectory() throws IOException {
        Properties props = new Properties();
        props.load(new ClassPathResource("application.properties").getInputStream());

        Path path = Paths.get(props.getProperty("checklist-service.archive.ena.checklist.localcopydir"));
        if (!path.toFile().exists()) {
            return;
        }

        Files.list(path).forEach(filePath -> {
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
