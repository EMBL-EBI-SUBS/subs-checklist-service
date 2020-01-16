package uk.ac.ebi.subs.checklistservice.test.repositories;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.ac.ebi.subs.repository.model.ArchivedChecklist;
import uk.ac.ebi.subs.repository.repos.ArchivedChecklistRepository;

import java.util.List;
import java.util.Optional;

public class AdapterArchivedChecklistRepository implements ArchivedChecklistRepository {

    @Override
    public <S extends ArchivedChecklist> S save(S entity) {
        return null;
    }

    @Override
    public <S extends ArchivedChecklist> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<ArchivedChecklist> findById(String s) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String s) {
        return false;
    }

    @Override
    public List<ArchivedChecklist> findAll() {
        return null;
    }

    @Override
    public Iterable<ArchivedChecklist> findAllById(Iterable<String> strings) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String s) {

    }

    @Override
    public void delete(ArchivedChecklist entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends ArchivedChecklist> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public List<ArchivedChecklist> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<ArchivedChecklist> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public <S extends ArchivedChecklist> S insert(S entity) {
        return null;
    }

    @Override
    public <S extends ArchivedChecklist> List<S> insert(Iterable<S> entities) {
        return null;
    }

    @Override
    public <S extends ArchivedChecklist> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends ArchivedChecklist> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends ArchivedChecklist> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends ArchivedChecklist> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends ArchivedChecklist> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends ArchivedChecklist> boolean exists(Example<S> example) {
        return false;
    }
}
