package aq.project.repositories.wallet_service;

import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface WalletServiceTransferTransactionRequestRepository extends CrudRepository<WalletServiceTransferTransactionRequest, UUID> {

    @Query(
            "SELECT request FROM WalletServiceTransferTransactionRequest request " +
                    "WHERE request.transactionRequestMetadata.isProcessed = false"
    )
    List<WalletServiceTransferTransactionRequest> findUnprocessedRequests();
}
