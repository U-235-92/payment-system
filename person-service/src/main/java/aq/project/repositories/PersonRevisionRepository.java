package aq.project.repositories;

import aq.project.entities.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRevisionRepository {

    List<Person> findAllRevisionsByKeycloakId(String keycloakId);

    Optional<Person> findUndoDeleteRevisionByPersonKeycloakId(String personKeycloakId);

    Optional<Person> findUndoUpdateRevisionByPersonKeycloakId(String personKeycloakId);
}
