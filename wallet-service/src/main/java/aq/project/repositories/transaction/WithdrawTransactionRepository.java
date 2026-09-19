package aq.project.repositories.transaction;

import aq.project.entities.transaction.WithdrawTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface WithdrawTransactionRepository extends
        PagingAndSortingRepository<WithdrawTransaction, UUID>,
        CrudRepository<WithdrawTransaction, UUID> {
}
