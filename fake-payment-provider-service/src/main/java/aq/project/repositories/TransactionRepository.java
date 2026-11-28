package aq.project.repositories;

import aq.project.dto.Status;
import aq.project.entities.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends CrudRepository<Transaction, UUID> {

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
    Optional<Transaction> findByIdAndMerchantId(UUID id, String merchantId);

    @Query(
            "SELECT t " +
            "FROM Transaction t " +
            "JOIN FETCH t.merchant m " +
            "WHERE m.merchantId = :merchantId " +
            "AND t.externalId = :externalId"
    )
    Optional<Transaction> findByExternalId(String externalId, String merchantId);

    @Query(
            "SELECT t " +
            "FROM Transaction t " +
            "JOIN FETCH t.merchant m " +
            "WHERE t.status = :status"
    )
    List<Transaction> findAllByStatus(Status status);
}