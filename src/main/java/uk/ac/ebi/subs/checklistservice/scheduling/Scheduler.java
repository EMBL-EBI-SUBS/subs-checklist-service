package uk.ac.ebi.subs.checklistservice.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.ebi.subs.checklistservice.services.ChecklistManager;

@Component
public class Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    @Autowired
    private ChecklistManager checklistManager;

    @Scheduled(cron = "0 30 01 * * ?")
    public void updateChecklists() {
        LOGGER.debug("Updating checklist start.");

        checklistManager.updateChecklists();

        LOGGER.debug("Updating checklist end.");
    }
}
