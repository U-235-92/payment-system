package aq.project.repositories.transaction_service;

import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TransactionServiceTransferTransactionRepository extends CrudRepository<TransactionServiceTransferTransaction, UUID> {
}
