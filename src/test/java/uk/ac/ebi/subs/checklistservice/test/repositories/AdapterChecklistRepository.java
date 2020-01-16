package uk.ac.ebi.subs.checklistservice.test.repositories;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.ac.ebi.subs.repository.model.Checklist;
import uk.ac.ebi.subs.repository.repos.ChecklistRepository;

import java.util.List;
import java.util.Optional;

public class AdapterChecklistRepository implements ChecklistRepository {
    @Override
    public List<Checklist> findByDataTypeId(String s) {
        return null;
    }

    @Override
    public <S extends Checklist> S save(S s) {
        return null;
    }

    @Override
    public <S extends Checklist> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Checklist> findById(String s) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String s) {
        return false;
    }

    @Override
    public List<Checklist> findAll() {
        return null;
    }

    @Override
    public Iterable<Checklist> findAllById(Iterable<String> strings) {
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
    public List<Checklist> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Checklist> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Checklist> S insert(S s) {
        return null;
    }

    @Override
    public <S extends Checklist> List<S> insert(Iterable<S> entities) {
        return null;
    }

    @Override
    public <S extends Checklist> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Checklist> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Checklist> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Checklist> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Checklist> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Checklist> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public void delete(Checklist checklist) {

    }

    @Override
    public void deleteAll(Iterable<? extends Checklist> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
