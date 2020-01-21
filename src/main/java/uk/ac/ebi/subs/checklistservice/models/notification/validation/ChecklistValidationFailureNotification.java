package uk.ac.ebi.subs.checklistservice.models.notification.validation;

import uk.ac.ebi.subs.repository.model.Checklist;

public abstract class ChecklistValidationFailureNotification {

    private Checklist checklist;

    public Checklist getChecklist() {
        return checklist;
    }

    public void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }
}
