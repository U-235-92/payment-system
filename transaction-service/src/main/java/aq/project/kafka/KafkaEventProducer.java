package aq.project.kafka;

import aq.project.entities.Transaction;
import aq.project.kafka.events.DepositTransactionRequestEvent;
import aq.project.kafka.events.WithdrawalTransactionRequestEvent;
import aq.project.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    @Value("{app.kafka.topics.deposit_transaction_request.name}")
    private String depositTransactionRequestTopic;
    @Value("${app.kafka.topics.withdrawal_transaction_request.name}")
    private String withdrawalTransactionRequestTopic;

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDepositTransactionRequestEvent(
            Transaction transaction
    ) {
        DepositTransactionRequestEvent event = transactionMapper.toDepositTransactionRequestEvent(transaction);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(depositTransactionRequestTopic, event);

        handleWhenComplete(transaction, future);
    }

    public void sendWithdrawalTransactionRequestEvent(
            Transaction transaction
    ) {
        WithdrawalTransactionRequestEvent event = transactionMapper.toWithdrawalTransactionRequestEvent(transaction);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(withdrawalTransactionRequestTopic, event);

        handleWhenComplete(transaction, future);
    }

    private void handleWhenComplete(
            Transaction transaction,
            CompletableFuture<SendResult<String, Object>> future
    ) {
        future.whenComplete((result, exception) -> {
            if(exception == null) {
                log.info("Complete send [{}] transaction request event " +
                                "with transaction id: [{}] to topic: [{}], offset: [{}]",
                        transaction.getPaymentType(),
                        transaction.getId(),
                        depositTransactionRequestTopic,
                        result.getRecordMetadata().offset());

            } else {
                log.error("Error occurred while sending [{}] transaction request event " +
                                "with transaction id: [{}] to topic: [{}]",
                        transaction.getPaymentType(),
                        transaction.getId(),
                        depositTransactionRequestTopic);
            }
        });
    }
}
