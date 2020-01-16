package uk.ac.ebi.subs.checklistservice.models.notification.validation;

import com.fasterxml.jackson.databind.JsonNode;

public class ChecklistSchemaValidationFailureNotification extends ChecklistValidationFailureNotification {

    private JsonNode schema;

    private JsonNode data;

    private String errorText;

    public JsonNode getSchema() {
        return schema;
    }

    public void setSchema(JsonNode schema) {
        this.schema = schema;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
