package aq.project.repositories;

import aq.project.entities.Transaction;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TransactionRepository extends
        CrudRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction> {
}
