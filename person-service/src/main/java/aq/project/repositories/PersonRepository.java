package aq.project.repositories;

import aq.project.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID>, RevisionRepository<Person, UUID, Integer>, PersonRevisionRepository {

    @Query("SELECT p FROM Person p JOIN p.individual i WHERE i.email = :email")
    Optional<Person> findByEmail(@Param("email") String email);

    @Query("SELECT p FROM Person p WHERE p.keycloakId = :keycloakId")
    Optional<Person> findByKeycloakId(@Param("keycloakId") String keycloakId);

    @Query("SELECT i.email FROM Person p JOIN p.individual i WHERE p.keycloakId = :keycloakId")
    Optional<String> findEmailByKeycloakId(@Param("keycloakId") String keycloakId);
}
