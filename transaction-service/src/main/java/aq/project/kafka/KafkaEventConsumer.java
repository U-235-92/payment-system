package aq.project.kafka;

import aq.project.dto.DepositWalletRequestDto;
import aq.project.dto.PaymentType;
import aq.project.dto.TransactionStatus;
import aq.project.dto.WithdrawalWalletRequestDto;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.kafka.events.TransactionResponseEvent;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TokenService;
import aq.project.utils.telemetry.TraceFactory;
import aq.project.wallet_service.WalletApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final TransactionRepository transactionRepository;

    private final WalletApiClient walletApiClient;

    private final TokenService tokenService;

    @Transactional
    @KafkaListener(
            topics = {
                    "${app.kafka.topics.deposit_transaction_complete_response.name}",
                    "${app.kafka.topics.withdrawal_transaction_complete_response.name}"
            }
    )
    public void handleCompleteTransactionEvent(
            TransactionResponseEvent transactionResponseEvent
    ) {
        Object[] transactionEventData = getTransactionEventData(transactionResponseEvent);

        UUID transactionId = (UUID) transactionEventData[0];

        TransactionStatus transactionStatus = (TransactionStatus) transactionEventData[1];

        PaymentType paymentType = (PaymentType) transactionEventData[2];

        try {
            logOnReceive(paymentType, transactionId, transactionStatus);

            checkTransactionInPendingState(paymentType, transactionId, transactionStatus);

            Transaction transaction = getTransaction(transactionId);
            transaction.completeTransaction();

            log.info("[{}] transaction with id: [{}] was completed", paymentType, transactionId);

        } catch (Exception e) {
            logOnError(paymentType, transactionId, e);
            throw e;
        }
    }

    @Transactional
    @KafkaListener(
            topics = {
                    "${app.kafka.topics.deposit_transaction_fail_response.name}",
                    "${app.kafka.topics.withdrawal_transaction_fail_response.name}"
            }
    )
    public void handleFailTransactionEvent(
            TransactionResponseEvent transactionResponseEvent
    ) {
        Object[] transactionEventData = getTransactionEventData(transactionResponseEvent);

        UUID transactionId = (UUID) transactionEventData[0];

        TransactionStatus transactionStatus = (TransactionStatus) transactionEventData[1];

        PaymentType paymentType = (PaymentType) transactionEventData[2];

        String description =  (String) transactionEventData[4] == null
                ? "---"
                : (String) transactionEventData[4];

        try {
            logOnReceive(paymentType, transactionId, transactionStatus);

            checkTransactionInPendingState(paymentType, transactionId, transactionStatus);

            Transaction transaction = getTransaction(transactionId);
            transaction.failTransaction();

            String traceId = TraceFactory.genTraceId();
            String authorization = tokenService.getAdminJwtAsAuthorizationHeaderValue();

            switch (paymentType) {
                case DEPOSIT -> {
//                    Compensation on fail
                    WithdrawalWalletRequestDto dto = new WithdrawalWalletRequestDto()
                            .walletId(transaction.getWalletId())
                            .amount(transaction.getAmount())
                            .currencyCode(transaction.getCurrencyCode());

                    walletApiClient.withdrawalWallet(traceId, dto, authorization);
                }

                case WITHDRAWAL ->  {
//                    Compensation on fail
                    DepositWalletRequestDto dto = new DepositWalletRequestDto()
                            .walletId(transaction.getWalletId())
                            .amount(transaction.getAmount())
                            .currencyCode(transaction.getCurrencyCode());

                    walletApiClient.depositWallet(traceId, dto, authorization);
                }
            }

            log.info("[{}] transaction with id: [{}] was failed. Description: {}",
                    paymentType, transactionId, description);

        } catch (Exception e) {
            logOnError(paymentType, transactionId, e);
            throw e;
        }
    }

    private Object[] getTransactionEventData(
            TransactionResponseEvent transactionResponseEvent
    ) {
        UUID transactionId = transactionResponseEvent.transactionId();

        TransactionStatus currentTransactionEventStatus = transactionResponseEvent.transactionStatus();

        PaymentType paymentType = transactionResponseEvent.paymentType();

        BigDecimal amount = transactionResponseEvent.amount();

        String description = transactionResponseEvent.description();

        return new Object[]{ transactionId, currentTransactionEventStatus, paymentType, amount, description };
    }

    private void checkTransactionInPendingState(
            PaymentType paymentType,
            UUID transactionId,
            TransactionStatus transactionStatus
    ) {
        if(transactionStatus != TransactionStatus.PENDING) {
            log.warn("[{}] transaction with id: [{}] is not in [{}] state, current state: [{}]",
                    paymentType, transactionId, TransactionStatus.PENDING, transactionStatus);

            throw new IllegalStateException(
                    String.format("Received transaction event in [%s] status", transactionStatus));
        }
    }

    private Transaction getTransaction(
            UUID transactionId
    ) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id: [%s] not found", transactionId)));
    }

    private void logOnError(
            PaymentType paymentType,
            UUID transactionId,
            Exception e
    ) {
        log.error("Error occurred while handle [{}] transaction event with transaction id: [{}]. Error message: [{}]",
                paymentType, transactionId, e.getMessage());
    }

    private void logOnReceive(
            PaymentType paymentType,
            UUID transactionId,
            TransactionStatus transactionStatus
    ) {
        log.info("Received [{}] transaction event with transaction id: [{}] in state: [{}]",
                paymentType, transactionId, transactionStatus);
    }
}
