package aq.project.configurations;

import aq.project.configurations.properties.KafkaBackoffProperties;
import aq.project.configurations.properties.KafkaTopicProperties;
import aq.project.exceptions.*;
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
        handler.addNotRetryableExceptions(EntityAlreadyExistsException.class);
        handler.addNotRetryableExceptions(EntityNotFoundException.class);
        handler.addNotRetryableExceptions(ProhibitedOperationException.class);
        handler.addNotRetryableExceptions(ForeignMerchantTransactionException.class);
        handler.addNotRetryableExceptions(DeserializationException.class);
        handler.addNotRetryableExceptions(ExecutionException.class);
        handler.addNotRetryableExceptions(InterruptedException.class);
        handler.addNotRetryableExceptions(ConstraintViolationException.class);
        handler.addNotRetryableExceptions(DtoConstraintsException.class);
        return handler;
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, destinationResolver());
        deadLetterPublishingRecoverer.setThrowIfNoDestinationReturned(false); // Whether it should throw an exception in case destinationResolver() returns null TopicPartition
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
        return exc instanceof DeserializationException
                || exc instanceof ExecutionException
                || exc instanceof InterruptedException
                || exc instanceof ConstraintViolationException;
    }

    private BackOff backOff() {
        long waitDuration = kafkaBackoffProperties.getWaitDuration();
        double multiplier = kafkaBackoffProperties.getMultiplier();
        int maxAttempts = kafkaBackoffProperties.getMaxAttempts();

        ExponentialBackOff backOff = new ExponentialBackOff(waitDuration, multiplier);
        backOff.setMaxAttempts(maxAttempts);
        return backOff;
    }

//     REQUEST TOPICS
    @Bean
    public NewTopic createTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic createTransactionRequestDltTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_transaction_request_dlt");
        return createTopic(config);
    }

    @Bean
    public NewTopic failTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("fail_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic failTransactionRequestDltTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("fail_transaction_request_dlt");
        return createTopic(config);
    }

    @Bean
    public NewTopic cancelTransactionRequestTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("cancel_transaction_request");
        return createTopic(config);
    }

    @Bean
    public NewTopic cancelTransactionRequestDltTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("cancel_transaction_request_dlt");
        return createTopic(config);
    }

//     RESPONSE TOPICS
    @Bean
    public NewTopic createTransactionResponseTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_transaction_response");
        return createTopic(config);
    }

    @Bean
    public NewTopic createTransactionResponseExceptionsTopic() {
        KafkaTopicProperties.KafkaTopicConfiguration config = kafkaTopicProperties
                .getTopics()
                .get("create_transaction_response_exceptions");
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
