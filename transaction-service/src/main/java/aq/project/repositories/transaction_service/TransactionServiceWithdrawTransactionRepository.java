package aq.project.repositories.transaction_service;

import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TransactionServiceWithdrawTransactionRepository extends CrudRepository<TransactionServiceWithdrawTransaction, UUID> {
}
