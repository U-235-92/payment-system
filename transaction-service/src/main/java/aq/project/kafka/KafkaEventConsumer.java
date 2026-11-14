package aq.project.kafka;

import aq.project.dto.PaymentType;
import aq.project.dto.TransactionStatus;
import aq.project.kafka.events.TransactionResponseEvent;
import aq.project.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final TransactionService transactionService;

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

            transactionService.completeTransaction(transactionId);

            log.info("[{}] transaction with id: [{}] was completed", paymentType, transactionId);

        } catch (Exception e) {
            logOnError(paymentType, transactionId, e);
            throw e;
        }
    }

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

            transactionService.failTransaction(transactionId);

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

    private void logOnReceive(
            PaymentType paymentType,
            UUID transactionId,
            TransactionStatus transactionStatus
    ) {
        log.info("Received [{}] transaction event with transaction id: [{}] in state: [{}]",
                paymentType, transactionId, transactionStatus);
    }

    private void logOnError(
            PaymentType paymentType,
            UUID transactionId,
            Exception e
    ) {
        log.error("Error occurred while handle [{}] transaction event with transaction id: [{}]. Error message: [{}]",
                paymentType, transactionId, e.getMessage());
    }
}