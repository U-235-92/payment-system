package aq.project.repositories;

import aq.project.entities.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransactionRepository extends PagingAndSortingRepository<Transaction, String>, CrudRepository<Transaction, String> {
}
