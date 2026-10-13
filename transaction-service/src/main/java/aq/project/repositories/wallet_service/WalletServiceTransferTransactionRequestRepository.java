package aq.project.repositories.wallet_service;

import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WalletServiceTransferTransactionRequestRepository extends
        CrudRepository<WalletServiceTransferTransactionRequest, Long> {

    @Query(
            "SELECT request " +
            "FROM WalletServiceTransferTransactionRequest request " +
            "WHERE request.transactionRequestMetadata.isProcessed = false"
    )
    List<WalletServiceTransferTransactionRequest> findUnprocessedRequests();
}
