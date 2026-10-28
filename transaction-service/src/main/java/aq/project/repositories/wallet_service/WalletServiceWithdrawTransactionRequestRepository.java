package aq.project.repositories.wallet_service;

import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WalletServiceWithdrawTransactionRequestRepository extends
        CrudRepository<WalletServiceWithdrawTransactionRequest, Long> {

    @Query(
            "SELECT request " +
            "FROM WalletServiceWithdrawTransactionRequest request " +
            "WHERE request.transactionRequestMetadata.isProcessed = false"
    )
    List<WalletServiceWithdrawTransactionRequest> findUnprocessedRequests();
}
