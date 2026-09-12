package aq.project.repositories.transaction;

import aq.project.entities.transaction.TransferTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface TransferTransactionRepository extends
        PagingAndSortingRepository<TransferTransaction, UUID>,
        CrudRepository<TransferTransaction, UUID> {
}
