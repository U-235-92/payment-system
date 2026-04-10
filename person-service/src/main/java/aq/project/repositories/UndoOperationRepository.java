package aq.project.repositories;

import aq.project.entities.UndoOperation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UndoOperationRepository extends CrudRepository<UndoOperation, UUID> {

    @Query("SELECT u FROM UndoOperation u WHERE u.personKeycloakId = :personKeycloakId")
    Optional<UndoOperation> findByPersonKeycloakId(@Param("personKeycloakId") UUID personKeycloakId);
}
