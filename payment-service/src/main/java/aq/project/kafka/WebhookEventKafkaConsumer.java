package aq.project.kafka;

import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.kafka.events.WebhookEvent;
import aq.project.kafka.events.WebhookEventStatus;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventKafkaConsumer {

    private final PaymentRepository paymentRepository;

    private final TraceContext traceContext;

    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.payment-status-update-response}")
    public void onWebhookEvent(
            WebhookEvent webhookEvent,
            Acknowledgment acknowledgment
    ) {
        String traceId = (traceContext.getTraceId() == null)
                ? TraceFactory.genTraceId()
                : traceContext.getTraceId();

        UUID paymentId = webhookEvent.paymentId();

        WebhookEventStatus webhookEventStatus = webhookEvent.status();

        try {
            switch (webhookEventStatus) {
                case SUCCESS -> {
                    Payment payment = getPayment(paymentId);

                    if(isUnprocessedPaymentInProcessingState(payment)) {
                        payment.complete();
                        payment.markProcessed();

                        paymentRepository.save(payment);
                    }
                }

                case FAIL -> {
                    Payment payment = getPayment(paymentId);

                    if(isUnprocessedPaymentInProcessingState(payment) || isUnprocessedPaymentInFailState(payment)) {
                        payment.fail();
                        payment.markProcessed();

                        paymentRepository.save(payment);
                    }
                }
            }
        } catch (EntityNotFoundException e) {
            log.error("[{}]: {} occurred while update payment status. Exception message: {}",
                    traceId, e.getClass().getSimpleName(), e.getMessage());
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private Payment getPayment(UUID paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Payment with payment id: [%s] not found", paymentId)));
    }

    private boolean isUnprocessedPaymentInProcessingState(Payment payment) {
        return !payment.isProcessed()
                && payment.getStatus() == PaymentStatus.PROCESSING;
    }

    private boolean isUnprocessedPaymentInFailState(Payment payment) {
        return !payment.isProcessed()
                && payment.getStatus() == PaymentStatus.FAILED;
    }
}