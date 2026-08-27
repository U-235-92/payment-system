package aq.project.repositories;

import aq.project.entities.WithdrawTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface WithdrawTransactionRepository extends
        PagingAndSortingRepository<WithdrawTransaction, String>,
        CrudRepository<WithdrawTransaction, String> {
}
