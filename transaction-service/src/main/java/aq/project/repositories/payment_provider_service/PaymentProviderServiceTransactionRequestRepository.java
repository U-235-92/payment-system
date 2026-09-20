package aq.project.repositories.payment_provider_service;

import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentProviderServiceTransactionRequestRepository extends
        CrudRepository<PaymentProviderServiceTransactionRequest, UUID> {

    @Query(
            "SELECT request FROM PaymentProviderServiceTransactionRequest request " +
            "WHERE request.transactionRequestMetadata.isProcessed = false"
    )
    List<PaymentProviderServiceTransactionRequest> findUnprocessedRequests();
}
