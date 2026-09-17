package aq.project.configurations;

import aq.project.configurations.properties.KafkaBackoffProperties;
import aq.project.configurations.properties.KafkaTopicProperties;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaBackoffProperties kafkaBackoffProperties;
    private final KafkaTopicProperties kafkaTopicProperties;

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        DefaultErrorHandler handler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backOff());
        handler.addNotRetryableExceptions(DuplicateTransactionHandleException.class);
        handler.addNotRetryableExceptions(EntityConstraintsException.class);
        handler.addNotRetryableExceptions(EntityNotFoundException.class);
        handler.addNotRetryableExceptions(DeserializationException.class);
        handler.addNotRetryableExceptions(ExecutionException.class);
        handler.addNotRetryableExceptions(InterruptedException.class);
        handler.addNotRetryableExceptions(ConstraintViolationException.class);
        return handler;
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, destinationResolver());
        deadLetterPublishingRecoverer.setThrowIfNoDestinationReturned(false);
        return deadLetterPublishingRecoverer;
    }

    private BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver() {
        return (record, exc) -> {
            if(isDltException(exc))
                return new TopicPartition(record.topic() + "_dlt", record.partition());
            return null;
        };
    }

    private boolean isDltException(Exception exc) {
        return exc instanceof DeserializationException ||
                exc instanceof ExecutionException ||
                exc instanceof InterruptedException;
    }

    private BackOff backOff() {
        long waitDuration = kafkaBackoffProperties.getWaitDuration();
        double multiplier = kafkaBackoffProperties.getMultiplier();
        int maxAttempts = kafkaBackoffProperties.getMaxAttempts();

        ExponentialBackOff backOff = new ExponentialBackOff(waitDuration, multiplier);
        backOff.setMaxAttempts(maxAttempts);
        return backOff;
    }

//     RESPONSE TOPICS
    @Bean
    public NewTopic createDepositTransactionResponseTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_deposit_transaction_response");
        return createTopic(config);
    }

    @Bean
    public NewTopic createDepositTransactionResponseDltTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_deposit_transaction_response_dlt");
        return createTopic(config);
    }

    @Bean
    public NewTopic createDepositTransactionResponseExceptionsTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_deposit_transaction_response_exceptions");
        return createTopic(config);
    }

    @Bean
    public NewTopic createWithdrawTransactionResponseTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_withdraw_transaction_response");
        return createTopic(config);
    }

    @Bean
    public NewTopic createWithdrawTransactionResponseDltTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_withdraw_transaction_response_dlt");
        return createTopic(config);
    }

    @Bean
    public NewTopic createWithdrawTransactionResponseExceptionsTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_withdraw_transaction_response_exceptions");
        return createTopic(config);
    }

    @Bean
    public NewTopic createTransferTransactionResponseTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_transfer_transaction_response");
        return createTopic(config);
    }

    @Bean
    public NewTopic createTransferTransactionResponseDltTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_transfer_transaction_response_dlt");
        return createTopic(config);
    }

    @Bean
    public NewTopic createTransferTransactionResponseExceptionsTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_transfer_transaction_response_exceptions");
        return createTopic(config);
    }

//     REQUEST TOPICS
    @Bean
    public NewTopic createDepositTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_deposit_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic failDepositTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("fail_deposit_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic cancelDepositTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("cancel_deposit_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic createWithdrawTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_withdraw_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic failWithdrawTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("fail_withdraw_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic cancelWithdrawTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("cancel_withdraw_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic createTransferTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_transfer_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic failTransferTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("fail_transfer_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic cancelTransferTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("cancel_transfer_transaction_request");
        return createTopic(config);
    }

    private NewTopic createTopic(KafkaTopicProperties.KafkaTopicConfiguration config) {
        return TopicBuilder.name(config.getName())
                .partitions(config.getPartitions())
                .replicas(config.getReplicas())
                .config("min.insync.replicas", String.valueOf(config.getMinInsyncReplicas()))
                .build();
    }
}
