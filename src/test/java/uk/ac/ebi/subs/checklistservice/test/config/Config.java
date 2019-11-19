package uk.ac.ebi.subs.checklistservice.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.checklistservice.test.repositories.AdapterChecklistRepository;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Configuration
@ComponentScan(basePackages = {"uk.ac.ebi.subs.checklistservice"})
public class Config {

    @Bean
    public ChecklistRepository checklistRepository() {
        return new AdapterChecklistRepository() {
            private HashMap<String, Checklist> map = new HashMap<>();

            @Override
            public <S extends Checklist> S save(S entity) {
                map.put(entity.getId(), entity);

                return entity;
            }

            @Override
            public Checklist findOne(String s) {
                return map.get(s);
            }

            @Override
            public List<Checklist> findAll() {
                return new ArrayList<>(map.values());
            }

            @Override
            public void deleteAll() {
                map.clear();
            }
        };
    }
}
