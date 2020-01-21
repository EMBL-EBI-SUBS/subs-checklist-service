package uk.ac.ebi.subs.checklistservice.services.internal.validation.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.checklistservice.services.internal.validation.ChecklistDataProvider;
import uk.ac.ebi.subs.repository.model.Checklist;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ChecklistSchemaValidationDataProvider implements ChecklistDataProvider<JsonNode> {

    public static final String RESOURCES_LOCATION_PATTERN = "classpath:validation/samples/%s/*.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistSchemaValidationDataProvider.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<JsonNode> getData(Checklist checklist) {
        LOGGER.debug("Loading data for checklist : {}", checklist.getId());

        String dataResourcePath = String.format(RESOURCES_LOCATION_PATTERN, checklist.getId());

        try {
            Resource[] dataResources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                    .getResources(dataResourcePath);

            return Stream.of(dataResources).map(dataResource -> {
                try {
                    LOGGER.debug("Reading data for checklist : {}, Data file : {}",
                            checklist.getId(), dataResource.getFilename());

                    return objectMapper.readValue(dataResource.getInputStream(), JsonNode.class);
                } catch (IOException e) {
                    throw new RuntimeException("Error reading checklist data file : " + dataResource.getFilename(), e);
                }
            }).collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("Data not available for checklist : %s, data path : %s",
                    checklist.getId(), dataResourcePath), e);
        } catch (IOException e) {
            throw new RuntimeException("Error listing checklist data path : " + dataResourcePath, e);
        }
    }
}
