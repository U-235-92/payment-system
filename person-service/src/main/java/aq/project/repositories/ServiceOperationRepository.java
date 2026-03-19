package aq.project.repositories;

import aq.project.entities.ServiceOperation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ServiceOperationRepository extends CrudRepository<ServiceOperation, Long> {

    @Query("SELECT o_so FROM ServiceOperation o_so WHERE o_so.status = :status ORDER BY id DESC LIMIT 1")
    Optional<ServiceOperation> findLastServiceOperation(@Param("status") String status);
}
