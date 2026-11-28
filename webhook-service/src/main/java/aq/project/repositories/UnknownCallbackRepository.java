package aq.project.repositories;

import aq.project.entities.UnknownCallback;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface UnknownCallbackRepository extends CrudRepository<UnknownCallback, UUID> {

    @Query("SELECT uc FROM UnknownCallback uc WHERE uc.processed = false")
    List<UnknownCallback> findAllUnprocessed();
}
