package aq.project.kafka;

import aq.project.dto.OperationType;
import aq.project.entities.PaymentProviderCallback;
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

    @Value("${app.kafka.topics.payment-status-update-response.name}")
    private String paymentStatusUpdateResponseTopic;

    private final CallbackService callbackService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendWebhookEvent(
            PaymentProviderCallback paymentProviderCallback
    ) {
        WebhookEvent webhookEvent = webhookMapper.toWebhookEvent(paymentProviderCallback);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate
                .send(paymentStatusUpdateResponseTopic, webhookEvent);

        future.whenComplete((result, exception) -> {
            OperationType operationType = paymentProviderCallback.getOperationType();

            UUID transactionId = paymentProviderCallback.getTransactionId();

            if(exception == null) {
//                Sending event is finished with no errors.
//                Mark incoming callback as processed.
                callbackService.markProcessed(paymentProviderCallback);

                long offset = result.getRecordMetadata().offset();

                log.info("Complete send [{}] webhook event with transaction id: [{}] to topic: [{}], offset: [{}]",
                        operationType, transactionId, paymentStatusUpdateResponseTopic, offset);

            } else {
//                Sending event is finished with errors.
                log.error("Error occurred while sending [{}] webhook event with transaction id: [{}] to topic: [{}]",
                        operationType, transactionId, paymentStatusUpdateResponseTopic);
            }
        });
    }
}