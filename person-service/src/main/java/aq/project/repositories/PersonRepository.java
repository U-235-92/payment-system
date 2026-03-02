package aq.project.repositories;

import aq.project.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {

    @Query("SELECT p FROM Person p JOIN p.individual i WHERE i.email = :email")
    Optional<Person> findByEmail(@Param("email") String email);
}
