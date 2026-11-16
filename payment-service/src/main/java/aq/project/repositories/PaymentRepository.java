package aq.project.repositories;

import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends CrudRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(UUID transactionId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findAllByPaymentStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.processed = false")
    List<Payment> findAllUnprocessedByPaymentStatus(PaymentStatus status);
}