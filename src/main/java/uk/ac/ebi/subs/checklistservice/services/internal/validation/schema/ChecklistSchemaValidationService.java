package uk.ac.ebi.subs.checklistservice.services.internal.validation.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.checklistservice.models.ChecklistSchemaValidationException;
import uk.ac.ebi.subs.checklistservice.services.internal.validation.ChecklistValidationService;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.util.SchemaConverterFromMongo;
import uk.ac.ebi.subs.validator.schema.JsonSchemaValidationService;
import uk.ac.ebi.subs.validator.schema.model.JsonSchemaValidationError;

import java.util.List;

@Service
public class ChecklistSchemaValidationService implements ChecklistValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistSchemaValidationService.class);

    @Autowired
    private ChecklistSchemaValidationDataProvider dataProvider;

    @Autowired
    private JsonSchemaValidationService validationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void validate(Checklist checklist) {
        JsonNode schema = null;

        try {
            LOGGER.debug("Fixing checklist schema : {}", checklist.getId());

            schema = SchemaConverterFromMongo.fixStoredJson(checklist.getValidationSchema());

            final JsonNode finalSchema = schema;
            dataProvider.getData(checklist).parallelStream()
                    .forEach(validationData -> {
                        List<JsonSchemaValidationError> errors = null;
                        try {
                            errors = validationService.validate(finalSchema, validationData);
                        } catch (Exception ex) {
                            throw new ChecklistSchemaValidationException(ex, checklist, finalSchema, validationData);
                        }

                        if (!errors.isEmpty()) {
                            throw new ChecklistSchemaValidationException(
                                    createExceptionMessage(errors), checklist, finalSchema, validationData);
                        }
                    });
        } catch (ChecklistSchemaValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ChecklistSchemaValidationException(
                    "Error validating checklist : " + checklist.getId(), ex, checklist, schema, null);
        }
    }

    private String createExceptionMessage(List<JsonSchemaValidationError> errors) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(errors);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
