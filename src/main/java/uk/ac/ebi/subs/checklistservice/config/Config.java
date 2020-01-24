package uk.ac.ebi.subs.checklistservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.subs.validator.schema.JsonSchemaValidationService;

@EnableScheduling
@EnableMongoRepositories(basePackages = "uk.ac.ebi.subs")
@EnableMongoAuditing
@ComponentScan(basePackages = {"uk.ac.ebi.subs.checklistservice"})
@Configuration
public class Config {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    public JsonSchemaValidationService jsonSchemaValidationService(
            @Value("${validator.schema.url}") String jsonSchemaValidatorUrl,
            RestTemplate restTemplate) {
        return new JsonSchemaValidationService(jsonSchemaValidatorUrl, restTemplate);
    }
}
