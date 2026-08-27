package aq.project.repositories;

import aq.project.entities.TransferTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransferTransactionRepository extends
        PagingAndSortingRepository<TransferTransaction, String>,
        CrudRepository<TransferTransaction, String> {
}
