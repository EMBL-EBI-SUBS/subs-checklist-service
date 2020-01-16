package uk.ac.ebi.subs.checklistservice.models;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.subs.repository.model.Checklist;

public class ChecklistSchemaValidationException extends ChecklistValidationException {

    private final JsonNode schema;

    private final JsonNode data;

    public ChecklistSchemaValidationException(Checklist checklist, JsonNode schema, JsonNode data) {
        super(checklist);
        this.schema = schema;
        this.data = data;
    }

    public ChecklistSchemaValidationException(String message, Checklist checklist, JsonNode schema, JsonNode data) {
        super(message, checklist);
        this.schema = schema;
        this.data = data;
    }

    public ChecklistSchemaValidationException(String message, Throwable cause, Checklist checklist, JsonNode schema, JsonNode data) {
        super(message, cause, checklist);
        this.schema = schema;
        this.data = data;
    }

    public ChecklistSchemaValidationException(Throwable cause, Checklist checklist, JsonNode schema, JsonNode data) {
        super(cause, checklist);
        this.schema = schema;
        this.data = data;
    }

    public ChecklistSchemaValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Checklist checklist, JsonNode schema, JsonNode data) {
        super(message, cause, enableSuppression, writableStackTrace, checklist);
        this.schema = schema;
        this.data = data;
    }

    public JsonNode getSchema() {
        return schema;
    }

    public JsonNode getData() {
        return data;
    }
}
