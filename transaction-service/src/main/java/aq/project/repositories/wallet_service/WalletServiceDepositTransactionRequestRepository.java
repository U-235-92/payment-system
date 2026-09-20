package aq.project.repositories.wallet_service;

import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface WalletServiceDepositTransactionRequestRepository extends CrudRepository<WalletServiceDepositTransactionRequest, UUID> {

    @Query(
            "SELECT request FROM WalletServiceDepositTransactionRequest request " +
                    "WHERE request.transactionRequestMetadata.isProcessed = false"
    )
    List<WalletServiceDepositTransactionRequest> findUnprocessedRequests();
}
