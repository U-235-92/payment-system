package aq.project.repositories;

import aq.project.entities.Person;
import aq.project.exceptions.NotFoundRevisionException;

import java.util.List;

public interface PersonRevisionRepository {

    List<Person> findRevisionsByKeycloakId(String keycloakId);

    Person findLastRevisionByKeycloakId(String keycloakId) throws NotFoundRevisionException;

    Person findUndoRevisionByKeycloakId(String keycloakId) throws NotFoundRevisionException;
}
