package uk.ac.ebi.subs.checklistservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.context.ApplicationPidFileWriter;
import uk.ac.ebi.subs.checklistservice.services.ChecklistManager;

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
public class ChecklistServiceApplication {

    @Autowired
    private ChecklistManager checklistManager;

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication( ChecklistServiceApplication.class);
        ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter();
        springApplication.addListeners( applicationPidFileWriter );
        springApplication.run(args);
    }
}
