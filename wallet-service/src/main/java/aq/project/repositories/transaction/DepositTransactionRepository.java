package aq.project.repositories.transaction;

import aq.project.entities.transaction.DepositTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface DepositTransactionRepository extends
        PagingAndSortingRepository<DepositTransaction, UUID>,
        CrudRepository<DepositTransaction, UUID> {
}
