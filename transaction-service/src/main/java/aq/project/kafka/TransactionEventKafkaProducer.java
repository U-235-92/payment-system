package aq.project.kafka;

import aq.project.dto.PaymentType;
import aq.project.entities.Transaction;
import aq.project.kafka.events.DepositRequestEvent;
import aq.project.kafka.events.WithdrawalRequestEvent;
import aq.project.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventKafkaProducer {

    @Value("${app.kafka.topics.deposit-transaction-request.name}")
    private String depositTransactionRequestTopic;
    @Value("${app.kafka.topics.withdrawal-transaction-request.name}")
    private String withdrawalTransactionRequestTopic;

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDepositTransactionEvent(
            Transaction transaction
    ) {
        DepositRequestEvent event = transactionMapper.toDepositTransactionRequestEvent(transaction);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(depositTransactionRequestTopic, event);

        handleWhenComplete(transaction, future);
    }

    public void sendWithdrawalTransactionEvent(
            Transaction transaction
    ) {
        WithdrawalRequestEvent event = transactionMapper.toWithdrawalTransactionRequestEvent(transaction);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(withdrawalTransactionRequestTopic, event);

        handleWhenComplete(transaction, future);
    }

    private void handleWhenComplete(
            Transaction transaction,
            CompletableFuture<SendResult<String, Object>> future
    ) {
        future.whenComplete((result, exception) -> {
            PaymentType paymentType = transaction.getPaymentType();

            UUID transactionId = transaction.getId();

            if(exception == null) {
                long offset = result.getRecordMetadata().offset();

                log.info("Complete send [{}] request event with transaction id: [{}] to topic: [{}], offset: [{}]",
                        paymentType, transactionId, depositTransactionRequestTopic, offset);

            } else {
                log.error("Error occurred while sending [{}] request event with transaction id: [{}] to topic: [{}]",
                        paymentType, transactionId, depositTransactionRequestTopic);
            }
        });
    }
}