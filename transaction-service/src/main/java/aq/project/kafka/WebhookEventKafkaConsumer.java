package aq.project.kafka;

import aq.project.exceptions.EntityNotFoundException;
import aq.project.kafka.events.WebhookEvent;
import aq.project.kafka.events.WebhookEventStatus;
import aq.project.services.TransactionService;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventKafkaConsumer {

    private final TransactionService transactionService;

    private final TraceContext traceContext;

    @KafkaListener(topics = "${app.kafka.topics.payment-status-update-response.name}")
    public void onWebhookEvent(
            WebhookEvent webhookEvent,
            Acknowledgment acknowledgment
    ) {
        String traceId = (traceContext.getTraceId() == null)
                ? TraceFactory.genTraceId()
                : traceContext.getTraceId();

        UUID transactionId = webhookEvent.transactionId();

        WebhookEventStatus webhookEventStatus = webhookEvent.status();

        log.debug(">>>>> Received [{}] webhook event from Kafka for transaction with id: [{}]",
                webhookEventStatus, transactionId);

        try {
            switch (webhookEventStatus) {
                case SUCCESS -> transactionService.completeTransaction(transactionId);

                case FAIL -> transactionService.failTransaction(transactionId);

                default -> log.error("Received unknown operation status: [{}]", webhookEventStatus);
            }
        } catch (IllegalStateException | EntityNotFoundException e) {
            log.error("[{}]: {} occurred while update transaction status. Exception message: {}",
                    traceId, e.getClass().getSimpleName(), e.getMessage());
        } finally {
            acknowledgment.acknowledge();
        }
    }
}