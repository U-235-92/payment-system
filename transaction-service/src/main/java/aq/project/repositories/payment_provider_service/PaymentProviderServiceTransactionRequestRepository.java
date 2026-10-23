package aq.project.repositories.payment_provider_service;

import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PaymentProviderServiceTransactionRequestRepository extends
        CrudRepository<PaymentProviderServiceTransactionRequest, Long> {

    @Query(
            "SELECT request FROM PaymentProviderServiceTransactionRequest request " +
            "WHERE request.transactionRequestMetadata.isProcessed = false"
    )
    List<PaymentProviderServiceTransactionRequest> findUnprocessedRequests();
}
