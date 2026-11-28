package aq.project.repositories;

import aq.project.dto.Status;
import aq.project.entities.Payout;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayoutRepository extends CrudRepository<Payout, UUID> {

    @Query(
            "SELECT p " +
            "FROM Payout p " +
            "JOIN FETCH p.merchant m " +
            "WHERE m.merchantId = :merchantId"
    )
    List<Payout> findByMerchantId(String merchantId);

    @Query(
            "SELECT p " +
            "FROM Payout p " +
            "JOIN FETCH p.merchant m " +
            "WHERE m.merchantId = :merchantId " +
            "AND p.createdAt BETWEEN :startDate AND :endDate"
    )
    List<Payout> findByMerchantId(String merchantId, OffsetDateTime startDate, OffsetDateTime endDate);

    @Query(
            "SELECT p " +
            "FROM Payout p " +
            "JOIN FETCH p.merchant m " +
            "WHERE m.merchantId = :merchantId " +
            "AND p.id = :id"
    )
    Optional<Payout> findByIdAndMerchantId(UUID id, String merchantId);

    @Query(
            "SELECT p " +
            "FROM Payout p " +
            "JOIN FETCH p.merchant m " +
            "WHERE m.merchantId = :merchantId " +
            "AND p.externalId = :externalId"
    )
    Optional<Payout> findByExternalId(String externalId, String merchantId);

    @Query(
            "SELECT p " +
            "FROM Payout p " +
            "JOIN FETCH p.merchant m " +
            "WHERE p.status = :status"
    )
    List<Payout> findAllByStatus(Status status);
}