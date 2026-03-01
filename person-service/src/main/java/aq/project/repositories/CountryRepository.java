package aq.project.repositories;

import aq.project.entities.Individual;
import aq.project.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface IndividualRepository extends CrudRepository<Individual, String> {
}
