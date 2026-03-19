package aq.project.repositories;

import aq.project.entities.UndoEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UndoEventRepository extends CrudRepository<UndoEvent, UUID> {

    @Query("SELECT u FROM UndoEvent u WHERE u.personKeycloakId = :personKeycloakId")
    Optional<UndoEvent> findByPersonKeycloakId(@Param("personKeycloakId") UUID personKeycloakId);
}
