package aq.project.schedulers;

import aq.project.dto.PaymentStatus;
import aq.project.entities.Payment;
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
            fixedRateString = "${app.service.payment-service.schedulers.schedule-complete-payment}",
            timeUnit = TimeUnit.SECONDS
    )
    public void scheduleCompletePayment() {
        List<Payment> completePayments = paymentRepository.findAllUnprocessedByPaymentStatus(PaymentStatus.COMPLETED);

        for(Payment completePayment : completePayments) {
            try {
                paymentService.handleCompletePayment(completePayment);
            } catch (Exception e) {
                onProcessPaymentException(completePayment, e);
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
                paymentService.handleFailPayment(failPayment);
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
