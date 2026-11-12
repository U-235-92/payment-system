package aq.project.repositories;

import aq.project.entities.Payout;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PayoutRepository extends CrudRepository<Payout, Long> {

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
    Optional<Payout> findByIdAndMerchantId(Long id, String merchantId);
}
