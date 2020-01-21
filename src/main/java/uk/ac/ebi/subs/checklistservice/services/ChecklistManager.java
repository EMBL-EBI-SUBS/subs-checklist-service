package uk.ac.ebi.subs.checklistservice.services;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.checklistservice.models.ChecklistSchemaValidationException;
import uk.ac.ebi.subs.checklistservice.models.ChecklistValidationException;
import uk.ac.ebi.subs.checklistservice.models.notification.validation.ChecklistSchemaValidationFailureNotification;
import uk.ac.ebi.subs.checklistservice.models.notification.validation.ChecklistValidationFailureNotification;
import uk.ac.ebi.subs.checklistservice.services.internal.archive.checklist.ArchiveChecklistService;
import uk.ac.ebi.subs.checklistservice.services.internal.notification.NotificationService;
import uk.ac.ebi.subs.checklistservice.services.internal.validation.ChecklistValidationService;
import uk.ac.ebi.subs.repository.model.ArchivedChecklist;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.repos.ArchivedChecklistRepository;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ChecklistManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistManager.class);

    @Autowired
    private List<ArchiveChecklistService> archiveChecklistServices;

    @Autowired
    private ChecklistValidationService validationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private ArchivedChecklistRepository archivedChecklistRepository;

    public void updateChecklists() {
        archiveChecklistServices.stream()
                .flatMap(archiveChecklistService -> archiveChecklistService.getUpdatedChecklists().stream())
                .map(this::validateChecklist)
                .filter(Objects::nonNull)
                .forEach(this::updateChecklist);
    }

    private Checklist validateChecklist(Checklist checklist) {
        LOGGER.debug("Validating checklist : {}", checklist.getId());

        try {
            validationService.validate(checklist);

            return checklist;
        } catch (ChecklistValidationException ex) {
            LOGGER.error("Checklist validation failed. ChecklistID : {}", checklist.getId(), ex);

            onChecklistValidationFailure(checklist, ex);
        } catch (Exception ex) {
            LOGGER.error("Error validating checklist. ChecklistID : {}", checklist, ex);
        }

        return null;
    }

    private void updateChecklist(Checklist checklist) {
        LOGGER.debug("Updating checklist : {}", checklist.getId());

        Optional<Checklist> currentOpt = checklistRepository.findById(checklist.getId());
        currentOpt.ifPresent(current -> {
            checklist.setCreatedDate(current.getCreatedDate());
            checklist.setLastModifiedDate(current.getLastModifiedDate());
            checklist.setVersion(current.getVersion() + 1);

            ArchivedChecklist archivedChecklist = new ArchivedChecklist();
            archivedChecklist.setChecklist(current);

            LOGGER.debug("Archiving existing checklist : {}, version : {}", current.getId(), current.getVersion());

            archivedChecklistRepository.save(archivedChecklist);

            LOGGER.debug("Existing checklist archived : {}, version : {}", current.getId(), current.getVersion());
        });

        checklistRepository.save(checklist);

        LOGGER.debug("Checklist updated : {}, version : {}", checklist.getId(), checklist.getVersion());
    }

    private void onChecklistValidationFailure(Checklist newChecklist, ChecklistValidationException ex) {
        Checklist current = checklistRepository.findById(newChecklist.getId()).orElse(null);
        if (current != null) {
            current.setOutdated(Boolean.TRUE);

            LOGGER.debug("Marking existing checklist as outdated. ChecklistID : {}", newChecklist.getId());

            checklistRepository.save(current);
        }

        LOGGER.debug("Sending validation failure notification. ChecklistID : {}", newChecklist.getId());

        try {
            notificationService.sendChecklistValidationFailureNotification(createNotification(ex));
        } catch (Exception exception) {
            LOGGER.error("Error sending validation failure notification. ChecklistID : {}",
                    newChecklist.getId(), exception);
        }
    }

    private ChecklistValidationFailureNotification createNotification(ChecklistValidationException ex) {
        if (ex instanceof ChecklistSchemaValidationException) {
            ChecklistSchemaValidationFailureNotification notification = new ChecklistSchemaValidationFailureNotification();
            notification.setChecklist(ex.getChecklist());
            notification.setSchema(((ChecklistSchemaValidationException) ex).getSchema());
            notification.setData(((ChecklistSchemaValidationException) ex).getData());
            notification.setErrorText(ExceptionUtils.getStackTrace(ex));

            return notification;
        } else {
            throw new RuntimeException("Unhandled checklist validation error.");
        }
    }
}
