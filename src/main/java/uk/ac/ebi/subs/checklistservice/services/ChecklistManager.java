package uk.ac.ebi.subs.checklistservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.checklistservice.services.internal.archivechecklists.ArchiveChecklistService;

import java.util.List;

@Service
public class ChecklistManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistManager.class);

    @Autowired
    private List<ArchiveChecklistService> archiveChecklistServices;

    public void updateChecklists() {
        archiveChecklistServices.stream()
                .flatMap(archiveChecklistService -> archiveChecklistService.getUpdatedChecklists().stream())
                .forEach(checklistId -> {
                    LOGGER.debug("Checklist updated : ChecklistID : {}", checklistId);
                });
    }
}
