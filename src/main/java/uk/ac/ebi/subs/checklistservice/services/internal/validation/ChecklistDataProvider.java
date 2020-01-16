package uk.ac.ebi.subs.checklistservice.services.internal.validation;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.repository.model.Checklist;

import java.util.List;

@Service
public interface ChecklistDataProvider<T> {

    List<T> getData(Checklist checklist);
}
