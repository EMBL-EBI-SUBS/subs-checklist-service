package uk.ac.ebi.subs.checklistservice.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.checklistservice.test.repositories.AdapterArchivedChecklistRepository;
import uk.ac.ebi.subs.checklistservice.test.repositories.AdapterChecklistRepository;
import uk.ac.ebi.subs.repository.model.ArchivedChecklist;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.repos.ArchivedChecklistRepository;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Configuration
@ComponentScan(basePackages = {"uk.ac.ebi.subs.checklistservice", "uk.ac.ebi.subs.validator.schema"})
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
            public List<Checklist> findAll() {
                return new ArrayList<>(map.values());
            }

            @Override
            public Optional<Checklist> findById(String s) {
                Checklist res = map.get(s);

                return res == null ? Optional.empty() : Optional.of(res);
            }

            @Override
            public void deleteAll() {
                map.clear();
            }
        };
    }

    @Bean
    public ArchivedChecklistRepository archivedChecklistRepository() {
        return new AdapterArchivedChecklistRepository() {
            private HashMap<String, ArchivedChecklist> map = new HashMap<>();

            @Override
            public <S extends ArchivedChecklist> S save(S entity) {
                map.put(entity.getId(), entity);

                return entity;
            }

            @Override
            public List<ArchivedChecklist> findAll() {
                return new ArrayList<>(map.values());
            }

            @Override
            public Optional<ArchivedChecklist> findById(String s) {
                ArchivedChecklist res = map.get(s);

                return res == null ? Optional.empty() : Optional.of(res);
            }

            @Override
            public void deleteAll() {
                map.clear();
            }
        };
    }
}
