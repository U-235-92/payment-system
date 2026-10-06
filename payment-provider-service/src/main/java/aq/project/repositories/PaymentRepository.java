package aq.project.repositories;

import aq.project.entities.Payment;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends CrudRepository<Payment, UUID> {

    Optional<Payment> findByTransactionId(UUID transactionId);
}
