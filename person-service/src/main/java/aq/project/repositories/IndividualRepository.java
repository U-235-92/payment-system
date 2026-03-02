package aq.project.repositories;

import aq.project.entities.Individual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IndividualRepository extends JpaRepository<Individual, UUID> {
}
