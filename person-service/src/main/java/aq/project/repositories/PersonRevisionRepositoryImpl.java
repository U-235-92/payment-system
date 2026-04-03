package aq.project.repositories;

import aq.project.entities.Person;
import aq.project.exceptions.NotFoundRevisionException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.envers.AuditReader;
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
    public List<Person> findRevisionsByKeycloakId(String keycloakId) {
        EntityManager entityManager = createEntityManager();
        try {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            return reader.createQuery()
                    .forRevisionsOfEntity(Person.class, true, true)
                    .add(AuditEntity.property("keycloakId").eq(keycloakId))
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Person findLastRevisionByKeycloakId(String keycloakId) throws NotFoundRevisionException {
        EntityManager entityManager = createEntityManager();
        try {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            int lastRevisionId = getLastRevisionId(entityManager);
            return new Person(Optional
                    .of((Person) reader.createQuery()
                            .forRevisionsOfEntity(Person.class, true, true)
                            .add(AuditEntity.revisionNumber().eq(lastRevisionId))
                            .getSingleResult())
                    .get());
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Person findUndoRevisionByKeycloakId(String keycloakId) throws NotFoundRevisionException {
        EntityManager entityManager = createEntityManager();
        try {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            int undoRevisionId = getLastRevisionId(entityManager) - 1;
            if(undoRevisionId <= 1)
                throw new NotFoundRevisionException("No undo revision found");
            return new Person(Optional
                    .ofNullable((Person) reader.createQuery()
                            .forRevisionsOfEntity(Person.class, true, true)
                            .add(AuditEntity.revisionNumber().eq(undoRevisionId))
                            .getSingleResult())
                    .orElseThrow(this::getLackRevisionException));
        } finally {
            entityManager.close();
        }
    }

    private int getLastRevisionId(EntityManager entityManager) throws NotFoundRevisionException {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        return Optional
                .ofNullable((Integer) reader.createQuery()
                    .forRevisionsOfEntity(Person.class, true, true)
                    .addProjection(AuditEntity.revisionNumber().max())
                    .getSingleResult())
                .orElseThrow(this::getLackRevisionException);
    }

    private NotFoundRevisionException getLackRevisionException() {
        return new NotFoundRevisionException("Revision table is empty");
    }

    private EntityManager createEntityManager() {
        EntityManagerFactory emf = (EntityManagerFactory)
                applicationContext.getBean("entityManagerFactory", EntityManagerFactory.class);
        return emf.createEntityManager();
    }
}
