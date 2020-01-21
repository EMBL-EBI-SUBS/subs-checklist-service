package uk.ac.ebi.subs.checklistservice.services.internal.validation;

import uk.ac.ebi.subs.repository.model.Checklist;

public interface ChecklistValidationService {
    void validate(Checklist checklist);
}
