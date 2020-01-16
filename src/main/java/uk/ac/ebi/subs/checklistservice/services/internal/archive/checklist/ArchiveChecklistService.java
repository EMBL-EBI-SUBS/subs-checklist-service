package uk.ac.ebi.subs.checklistservice.services.internal.archive.checklist;

import uk.ac.ebi.subs.repository.model.Checklist;

import java.util.List;

public interface ArchiveChecklistService {
    List<Checklist> getUpdatedChecklists();
}
