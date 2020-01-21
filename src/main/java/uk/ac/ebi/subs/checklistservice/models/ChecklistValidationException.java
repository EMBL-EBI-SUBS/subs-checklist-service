package uk.ac.ebi.subs.checklistservice.models;

import uk.ac.ebi.subs.repository.model.Checklist;

public abstract class ChecklistValidationException extends RuntimeException {
    private final Checklist checklist;

    public ChecklistValidationException(Checklist checklist) {
        this.checklist = checklist;
    }

    public ChecklistValidationException(String message, Checklist checklist) {
        super(message);
        this.checklist = checklist;
    }

    public ChecklistValidationException(String message, Throwable cause, Checklist checklist) {
        super(message, cause);
        this.checklist = checklist;
    }

    public ChecklistValidationException(Throwable cause, Checklist checklist) {
        super(cause);
        this.checklist = checklist;
    }

    public ChecklistValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Checklist checklist) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.checklist = checklist;
    }

    public Checklist getChecklist() {
        return checklist;
    }
}
