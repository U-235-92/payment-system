package aq.project.utils.schedulers;

import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.repositories.wallet_service.WalletServiceTransferTransactionRequestRepository;
import aq.project.repositories.wallet_service.WalletServiceWithdrawTransactionRequestRepository;
import aq.project.utils.handlers.wallet_service.request.WalletServiceDepositTransactionRequestHandler;
import aq.project.utils.handlers.wallet_service.request.WalletServiceTransferTransactionRequestHandler;
import aq.project.utils.handlers.wallet_service.request.WalletServiceWithdrawTransactionRequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WalletServiceCreateTransactionScheduler {

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
    public void scheduleHandleCreateDepositTransactionOnWalletService() {
        List<WalletServiceDepositTransactionRequest> transactionRequests = walletServiceDepositTransactionRequestRepository.findUnprocessedRequests();
        for(WalletServiceDepositTransactionRequest transactionRequest : transactionRequests) {
            walletServiceDepositTransactionRequestHandler.handleTransactionRequest(transactionRequest);
        }
    }

    @Scheduled(
            fixedRateString = "${service.transaction-service.schedule.handle-create-withdraw-transaction-on-wallet-service}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void scheduleHandleCreateWithdrawTransactionOnWalletService() {
        List<WalletServiceWithdrawTransactionRequest> transactionRequests = walletServiceWithdrawTransactionRequestRepository.findUnprocessedRequests();
        for(WalletServiceWithdrawTransactionRequest transactionRequest : transactionRequests) {
            walletServiceWithdrawTransactionRequestHandler.handleTransactionRequest(transactionRequest);
        }
    }

    @Scheduled(
            fixedRateString = "${service.transaction-service.schedule.handle-create-transfer-transaction-on-wallet-service}",
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void scheduleHandleCreateTransferTransactionOnWalletService() {
        List<WalletServiceTransferTransactionRequest> transactionRequests = walletServiceTransferTransactionRequestRepository.findUnprocessedRequests();
        for(WalletServiceTransferTransactionRequest transactionRequest : transactionRequests) {
            walletServiceTransferTransactionRequestHandler.handleTransactionRequest(transactionRequest);
        }
    }
}
