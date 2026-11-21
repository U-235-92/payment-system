package aq.project.repositories;

import aq.project.entities.PaymentProviderCallback;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentProviderCallbackRepository extends CrudRepository<PaymentProviderCallback, UUID> {

    @Query("SELECT ppc FROM PaymentProviderCallback ppc WHERE ppc.processed = false")
    List<PaymentProviderCallback> findAllUnprocessed();

    @Query("SELECT ppc FROM PaymentProviderCallback ppc WHERE ppc.transactionId = :transactionId")
    Optional<PaymentProviderCallback> findByTransactionId(UUID transactionId);
}
