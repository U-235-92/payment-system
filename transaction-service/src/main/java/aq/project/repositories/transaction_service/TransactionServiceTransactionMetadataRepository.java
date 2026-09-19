package aq.project.repositories;

import aq.project.entities.transaction_service.TransactionServiceTransactionMetadata;
import org.springframework.data.repository.CrudRepository;

public interface TransactionMetadataRepository extends CrudRepository<TransactionServiceTransactionMetadata, Long> {
}
