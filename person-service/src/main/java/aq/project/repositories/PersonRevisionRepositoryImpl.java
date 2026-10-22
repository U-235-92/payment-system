package aq.project.repositories;

import aq.project.entities.Person;
import aq.project.exceptions.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PersonRevisionRepositoryImpl implements PersonRevisionRepository {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public Optional<Person> findUndoDeleteRevisionByPersonKeycloakId(String personKeycloakId) {
        try(EntityManager entityManager = createEntityManager()) {
            Person restoredPerson = (Person) AuditReaderFactory.get(entityManager).createQuery()
                    .forRevisionsOfEntity(Person.class, true, true)
                    .add(AuditEntity.revisionNumber().eq(getMaxRevisionNumber(personKeycloakId)))
                    .getSingleResult();

            return getOptionalPerson(personKeycloakId, restoredPerson);
        }
    }

    @Override
    public Optional<Person> findUndoUpdateRevisionByPersonKeycloakId(String personKeycloakId) {
        try(EntityManager entityManager = createEntityManager()) {
            Person restoredPerson = (Person) AuditReaderFactory.get(entityManager).createQuery()
                    .forRevisionsOfEntity(Person.class, true, true)
                    .add(AuditEntity.revisionNumber().eq(getMaxRevisionNumber(personKeycloakId) - 1))
                    .getSingleResult();

            return getOptionalPerson(personKeycloakId, restoredPerson);
        }
    }

    private Integer getMaxRevisionNumber(String personKeycloakId) {
        try(EntityManager entityManager = createEntityManager()) {
            return (Integer) AuditReaderFactory.get(entityManager).createQuery()
                    .forRevisionsOfEntity(Person.class, true, true)
                    .add(AuditEntity.property("keycloakId").eq(personKeycloakId))
                    .addProjection(AuditEntity.revisionNumber().max())
                    .getSingleResult();
        }
    }

    private Optional<Person> getOptionalPerson(
            String personKeycloakId,
            Person restoredPerson
    ) {
        return Optional.ofNullable(restoredPerson)
                .map(person -> Optional.of(new Person(person)))
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Not found revision for person with keycloak id: [%s]", personKeycloakId)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Person> findAllRevisionsByKeycloakId(String keycloakId) {
        try(EntityManager entityManager = createEntityManager()) {
            return AuditReaderFactory.get(entityManager).createQuery()
                    .forRevisionsOfEntity(Person.class, true, true)
                    .add(AuditEntity.property("keycloakId").eq(keycloakId))
                    .getResultList();
        }
    }

    private EntityManager createEntityManager() {
        EntityManagerFactory emf = applicationContext
                .getBean("entityManagerFactory", EntityManagerFactory.class);
        return emf.createEntityManager();
    }
}