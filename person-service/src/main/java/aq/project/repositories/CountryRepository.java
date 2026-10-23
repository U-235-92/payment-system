package aq.project.repositories;

import aq.project.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, String> {

    @Query("SELECT c FROM Country c WHERE c.code = :code")
    Optional<Country> findByCountryCode(@Param("code") String code);
}
