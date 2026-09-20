package aq.project.repositories.transaction_service;

import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TransactionServiceDepositTransactionRepository extends CrudRepository<TransactionServiceDepositTransaction, UUID> {
}
