package aq.project.repositories;

import aq.project.entities.Country;
import aq.project.entities.Individual;
import org.springframework.data.repository.CrudRepository;

public interface CountryRepository extends CrudRepository<Country, Long> {
}
