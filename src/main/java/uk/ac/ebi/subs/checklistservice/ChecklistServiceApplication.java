package uk.ac.ebi.subs.checklistservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.system.ApplicationPidFileWriter;
import uk.ac.ebi.subs.checklistservice.services.ChecklistManager;

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
//todo remove interface
public class ChecklistServiceApplication implements CommandLineRunner {

    @Autowired
    private ChecklistManager checklistManager;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication( ChecklistServiceApplication.class);
        ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter();
        springApplication.addListeners( applicationPidFileWriter );
        springApplication.run(args);
    }

    //todo remove this later
    @Override
    public void run(String... args) throws Exception {
        checklistManager.updateChecklists();
    }
}
