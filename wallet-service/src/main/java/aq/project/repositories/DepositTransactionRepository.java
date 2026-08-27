package aq.project.repositories;

import aq.project.entities.DepositTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DepositTransactionRepository extends
        PagingAndSortingRepository<DepositTransaction, String>,
        CrudRepository<DepositTransaction, String> {
}
