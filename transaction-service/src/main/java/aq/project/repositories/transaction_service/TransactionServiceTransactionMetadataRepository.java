package aq.project.repositories.transaction_service;

import aq.project.entities.transaction_service.TransactionServiceTransactionMetadata;
import org.springframework.data.repository.CrudRepository;

public interface TransactionServiceTransactionMetadataRepository extends CrudRepository<TransactionServiceTransactionMetadata, Long> {
}
