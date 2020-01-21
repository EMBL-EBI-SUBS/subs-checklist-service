package uk.ac.ebi.subs.checklistservice.services.internal.notification;

import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.checklistservice.models.notification.validation.ChecklistValidationFailureNotification;

@Service
public interface NotificationService {

    void sendChecklistValidationFailureNotification(ChecklistValidationFailureNotification notification);
}
