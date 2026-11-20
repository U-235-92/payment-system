package aq.project.kafka;

import aq.project.entities.Payment;
import aq.project.kafka.events.WebhookEvent;
import aq.project.mappers.WebhookMapper;
import aq.project.services.CallbackService;
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
public class WebhookEventKafkaProducer {

    private final WebhookMapper webhookMapper = WebhookMapper.INSTANCE;

    @Value("${app.kafka.topics.payment-status-update-response}")
    private String paymentStatusUpdateResponseTopic;

    private final CallbackService callbackService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendWebhookEvent(
            Payment payment
    ) {
        WebhookEvent webhookEvent = webhookMapper.toWebhookEvent(payment);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate
                .send(paymentStatusUpdateResponseTopic, webhookEvent);

        future.whenComplete((result, exception) -> {
            UUID transactionId = payment.getTransactionId();

            if(exception == null) {
                callbackService.markProcessed(payment);

                long offset = result.getRecordMetadata().offset();

                log.info("Complete send webhook event with transaction id: [{}] to topic: [{}], offset: [{}]",
                        transactionId, paymentStatusUpdateResponseTopic, offset);

            } else {
                log.error("Error occurred while sending webhook event with transaction id: [{}] to topic: [{}]",
                        transactionId, paymentStatusUpdateResponseTopic);
            }
        });
    }
}