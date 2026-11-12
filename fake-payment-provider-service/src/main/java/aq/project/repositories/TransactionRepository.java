package aq.project.repositories;

import aq.project.entities.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    @Query(
            "SELECT t " +
            "FROM Transaction t " +
            "JOIN FETCH t.merchant m " +
            "WHERE m.merchantId = :merchantId"
    )
    List<Transaction> findByMerchantId(String merchantId);

    @Query(
            "SELECT t " +
            "FROM Transaction t " +
            "JOIN FETCH t.merchant m " +
            "WHERE m.merchantId = :merchantId " +
            "AND t.createdAt BETWEEN :startDate AND :endDate"
    )
    List<Transaction> findByMerchantId(String merchantId, OffsetDateTime startDate, OffsetDateTime endDate);

    @Query(
            "SELECT t " +
            "FROM Transaction t " +
            "JOIN FETCH t.merchant m " +
            "WHERE m.merchantId = :merchantId " +
            "AND t.id = :id"
    )
    Optional<Transaction> findByIdAndMerchantId(Long id, String merchantId);
}
