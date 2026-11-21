package aq.project.repositories;

import aq.project.entities.VerificationCallback;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationCallbackRepository extends CrudRepository<VerificationCallback, UUID> {

    @Query("SELECT vc FROM VerificationCallback vc WHERE vc.processed = false")
    List<VerificationCallback> findAllUnprocessed();
}
