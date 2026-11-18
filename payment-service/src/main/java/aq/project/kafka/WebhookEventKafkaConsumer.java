package aq.project.kafka;

import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.kafka.events.WebhookEvent;
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
    @KafkaListener(topics = "${app.kafka.topics.payment_status_update_response}")
    public void onWebhookEvent(
            WebhookEvent webhookEvent,
            Acknowledgment acknowledgment
    ) {
        String traceId = (traceContext.getTraceId() == null)
                ? TraceFactory.genTraceId()
                : traceContext.getTraceId();

        UUID paymentId = webhookEvent.paymentId();

        PaymentStatus paymentStatus = null;

        try {
            paymentStatus = PaymentStatus.valueOf(webhookEvent.status());

        } catch (IllegalArgumentException e) {
            log.error("[{}]: Received invalid payment status value: [{}] for payment with payment id: [{}]. " +
                            "Exception message: {}",
                    traceId, paymentStatus, paymentId, e.getMessage());

            acknowledgment.acknowledge();

            return;
        }

        try {
            switch (paymentStatus) {
                case COMPLETED -> {
                    Payment payment = getPayment(paymentId);

                    if(!payment.isProcessed() && payment.getStatus() == PaymentStatus.PROCESSING) {
                        payment.complete();
                        payment.markProcessed();

                        paymentRepository.save(payment);
                    }
                }

                case FAILED -> {
                    Payment payment = getPayment(paymentId);

                    if(!payment.isProcessed() && payment.getStatus() == PaymentStatus.PROCESSING) {
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
}