package aq.project.schedulers;

import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
import aq.project.kafka.WebhookEventKafkaProducer;
import aq.project.repositories.PaymentRepository;
import aq.project.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;

    private final PaymentService paymentService;

    private final WebhookEventKafkaProducer webhookEventKafkaProducer;

    @Scheduled(
            fixedRateString = "${app.service.payment-service.schedulers.schedule-pending-payment}",
            timeUnit = TimeUnit.SECONDS
    )
    public void schedulePendingPayment() {
        List<Payment> pendingPayments = paymentRepository.findAllUnprocessedByPaymentStatus(PaymentStatus.PENDING);

        for(Payment pendingPayment : pendingPayments) {
            try {
                paymentService.handlePendingPayment(pendingPayment);
            } catch (Exception e) {
                onProcessPaymentException(pendingPayment, e);
            }
        }
    }

    @Scheduled(
            fixedRateString = "${app.service.payment-service.schedulers.schedule-processing-payment}",
            timeUnit = TimeUnit.SECONDS
    )
    public void scheduleProcessingPayment() {
        List<Payment> processingPayments = paymentRepository.findAllUnprocessedByPaymentStatus(PaymentStatus.PROCESSING);

        for(Payment processingPayment : processingPayments) {
            try {
                paymentService.handleProcessingPayment(processingPayment);
            } catch (Exception e) {
                onProcessPaymentException(processingPayment, e);
            }
        }
    }

    @Scheduled(
            fixedRateString = "${app.service.payment-service.schedulers.schedule-fail-payment}",
            timeUnit = TimeUnit.SECONDS
    )
    public void scheduleFailPayment() {
        List<Payment> failPayments = paymentRepository.findAllUnprocessedByPaymentStatus(PaymentStatus.FAILED);

        for(Payment failPayment : failPayments) {
            try {
                webhookEventKafkaProducer.sendWebhookEvent(failPayment);
            } catch (Exception e) {
                onProcessPaymentException(failPayment, e);
            }
        }
    }

    private void onProcessPaymentException(Payment payment, Exception e) {
        log.error("{} occurred while process payment in [{}] status with payment id: [{}]. Exception message: {}",
                e.getClass().getSimpleName(),
                payment.getStatus(),
                payment.getPaymentId(),
                e.getMessage());
    }
}