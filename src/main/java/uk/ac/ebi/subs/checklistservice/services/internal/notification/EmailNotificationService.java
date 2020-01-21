package uk.ac.ebi.subs.checklistservice.services.internal.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.checklistservice.models.notification.validation.ChecklistSchemaValidationFailureNotification;
import uk.ac.ebi.subs.checklistservice.models.notification.validation.ChecklistValidationFailureNotification;

import javax.mail.internet.MimeMessage;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

    @Value("${checklist-service.validation.notification.email.from}")
    private String from;

    @Value("${checklist-service.validation.notification.email.to}")
    private String to;

    @Value("${checklist-service.validation.notification.email.replyTo}")
    private String replyTo;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void sendChecklistValidationFailureNotification(ChecklistValidationFailureNotification notification) {
        if (notification instanceof ChecklistSchemaValidationFailureNotification) {
            sendSchemaValidationFailureNotification((ChecklistSchemaValidationFailureNotification) notification);
        } else {
            throw new RuntimeException("Unhandled checklist validation failure notification.");
        }
    }

    private void sendSchemaValidationFailureNotification(ChecklistSchemaValidationFailureNotification notification) {
        LOGGER.debug("Sending checklist schema validation failure email. ChecklistID : {}",
                notification.getChecklist().getId());

        MimeMessage mimeMessage = emailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setReplyTo(replyTo);
            mimeMessageHelper.setSubject(
                    String.format("Checklist '%s' validation failure", notification.getChecklist().getId()));
            mimeMessageHelper.setText(notification.getErrorText());

            if (notification.getSchema() != null) {
                mimeMessageHelper.addAttachment("schema.json", new ByteArrayResource(
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(notification.getSchema())), "application/json");
            }
            if (notification.getData() != null) {
                mimeMessageHelper.addAttachment("data.json", new ByteArrayResource(
                        objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(notification.getData())), "application/json");
            }

            emailSender.send(mimeMessage);

            LOGGER.debug("Checklist schema validation failure email sent. ChecklistID : {}",
                    notification.getChecklist().getId());
        } catch (Exception e) {
            throw new RuntimeException("Error sending schema validation failure email. ChecklistID : " +
                    notification.getChecklist().getId(), e);
        }
    }
}
