package aq.project.configurations;

import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.ExceedAttemptLimitException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.exceptions.ServiceHttpException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Value("${service.kafka.backoff.max-attempts}")
    private long maxAttempts;
    @Value("${service.kafka.backoff.wait-duration}")
    private long waitDuration;
    @Value("${service.kafka.backoff.multiplier}")
    private double multiplier;

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        DefaultErrorHandler handler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backOff());
        handler.addRetryableExceptions(ServiceHttpException.class);
        handler.addNotRetryableExceptions(ClientHttpException.class);
        handler.addNotRetryableExceptions(DeserializationException.class);
        handler.addNotRetryableExceptions(FallbackOperationException.class);
        handler.addNotRetryableExceptions(ExceedAttemptLimitException.class);
        handler.addNotRetryableExceptions(ExecutionException.class);
        handler.addNotRetryableExceptions(InterruptedException.class);
        handler.addNotRetryableExceptions(ConstraintViolationException.class);
        return handler;
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, destinationResolver());
    }

    private BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver() {
        return (record, exc) -> {
            if(exc instanceof ClientHttpException)
                return new TopicPartition(record.topic() + "_dlt", record.partition());
            return new TopicPartition(record.topic() + "_exceptions", record.partition());
        };
    }

    private BackOff backOff() {
        ExponentialBackOff backOff = new ExponentialBackOff(waitDuration, multiplier);
        backOff.setMaxAttempts(maxAttempts);
        return backOff;
    }
}
