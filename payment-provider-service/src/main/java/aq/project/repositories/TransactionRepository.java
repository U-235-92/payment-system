package aq.project.repositories;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, UUID> {

    List<Transaction> findByMerchantId(String merchantId);

    List<Transaction> findByMerchantIdAndMetadataCreatedAtBetween(String merchantId, OffsetDateTime startDate, OffsetDateTime endDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Transaction> findByStatus(TransactionStatus status);
}