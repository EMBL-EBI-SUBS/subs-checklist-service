package uk.ac.ebi.subs.checklistservice.services.internal.archivechecklists;

import uk.ac.ebi.subs.repository.model.Checklist;

import java.util.List;

public interface ArchiveChecklistService {
    List<String> getUpdatedChecklists();
}
