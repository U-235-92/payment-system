package aq.project.utils.schedulers;

import aq.project.dto.Operation;
import aq.project.dto.TransactionStatus;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.repositories.wallet_service.WalletServiceTransferTransactionRequestRepository;
import aq.project.repositories.wallet_service.WalletServiceWithdrawTransactionRequestRepository;
import aq.project.utils.handlers.wallet_service.request.WalletServiceDepositTransactionRequestHandler;
import aq.project.utils.handlers.wallet_service.request.WalletServiceTransferTransactionRequestHandler;
import aq.project.utils.handlers.wallet_service.request.WalletServiceWithdrawTransactionRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WalletServiceCreateTransactionScheduler {

    @Value("${service.wallet-service.name}")
    private String walletServiceName;

    private final WalletServiceDepositTransactionRequestRepository walletServiceDepositTransactionRequestRepository;
    private final WalletServiceWithdrawTransactionRequestRepository walletServiceWithdrawTransactionRequestRepository;
    private final WalletServiceTransferTransactionRequestRepository walletServiceTransferTransactionRequestRepository;

    private final WalletServiceDepositTransactionRequestHandler walletServiceDepositTransactionRequestHandler;
    private final WalletServiceWithdrawTransactionRequestHandler walletServiceWithdrawTransactionRequestHandler;
    private final WalletServiceTransferTransactionRequestHandler walletServiceTransferTransactionRequestHandler;

    @Scheduled(
            fixedRateString = "${service.transaction-service.schedule.handle-create-deposit-transaction-on-wallet-service}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void scheduleHandleDepositTransactionOnWalletService() {
        List<WalletServiceDepositTransactionRequest> transactionRequests = walletServiceDepositTransactionRequestRepository
                .findUnprocessedRequests();

        for(WalletServiceDepositTransactionRequest transactionRequest : transactionRequests) {
            Long requestId = transactionRequest.getId();
            TransactionStatus transactionStatus = transactionRequest.getStatus();

            Operation operation = Operation.DEPOSIT;

            switch (transactionStatus) {
                case PENDING -> walletServiceDepositTransactionRequestHandler
                        .handleCreateTransactionRequest(transactionRequest);

                case FAILED ->  walletServiceDepositTransactionRequestHandler
                        .handleFailTransactionRequest(transactionRequest);

                default -> throw new ProcessOperationException(String.format(
                        "Attempt to schedule [%s] transaction request with id: [%s] in status: [%s] " +
                        "for service: [%s] which is prohibited",
                        operation, requestId, transactionStatus, walletServiceName));
            }
        }
    }

    @Scheduled(
            fixedRateString = "${service.transaction-service.schedule.handle-create-withdraw-transaction-on-wallet-service}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void scheduleHandleWithdrawTransactionOnWalletService() {
        List<WalletServiceWithdrawTransactionRequest> transactionRequests = walletServiceWithdrawTransactionRequestRepository
                .findUnprocessedRequests();

        for(WalletServiceWithdrawTransactionRequest transactionRequest : transactionRequests) {
            Long requestId = transactionRequest.getId();
            TransactionStatus transactionStatus = transactionRequest.getStatus();

            Operation operation = Operation.WITHDRAW;

            switch (transactionStatus) {
                case PENDING -> walletServiceWithdrawTransactionRequestHandler
                        .handleCreateTransactionRequest(transactionRequest);

                case FAILED -> walletServiceWithdrawTransactionRequestHandler
                        .handleFailTransactionRequest(transactionRequest);

                default -> throw new ProcessOperationException(String.format(
                        "Attempt to schedule [%s] transaction request with id: [%s] in status: [%s] " +
                        "for service: [%s] which is prohibited",
                        operation, requestId, transactionStatus, walletServiceName));
            }
        }
    }

    @Scheduled(
            fixedRateString = "${service.transaction-service.schedule.handle-create-transfer-transaction-on-wallet-service}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void scheduleHandleTransferTransactionOnWalletService() {
        List<WalletServiceTransferTransactionRequest> transactionRequests = walletServiceTransferTransactionRequestRepository
                .findUnprocessedRequests();

        for(WalletServiceTransferTransactionRequest transactionRequest : transactionRequests) {
            Long requestId = transactionRequest.getId();
            TransactionStatus transactionStatus = transactionRequest.getStatus();

            Operation operation = Operation.TRANSFER;

            switch (transactionStatus) {
                case PENDING -> walletServiceTransferTransactionRequestHandler
                        .handleCreateTransactionRequest(transactionRequest);

                case FAILED -> walletServiceTransferTransactionRequestHandler
                        .handleFailTransactionRequest(transactionRequest);

                default -> throw new ProcessOperationException(String.format(
                        "Attempt to schedule [%s] transaction request with id: [%s] in status: [%s] " +
                        "for service: [%s] which is prohibited",
                        operation, requestId, transactionStatus, walletServiceName));
            }
        }
    }
}
