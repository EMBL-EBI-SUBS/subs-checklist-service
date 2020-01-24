package uk.ac.ebi.subs.checklistservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.system.ApplicationPidFileWriter;

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
public class ChecklistServiceApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication( ChecklistServiceApplication.class);
        ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter();
        springApplication.addListeners( applicationPidFileWriter );
        springApplication.run(args);
    }
}
