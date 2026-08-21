package aq.project.repositories;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, String> {

    List<Transaction> findByStatus(TransactionStatus transactionStatus);
}
