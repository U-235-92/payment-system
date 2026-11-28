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
            fixedRateString = "${app.service.payment-service.schedulers.schedule-pending-payment}",
            timeUnit = TimeUnit.SECONDS
    )
    public void schedulePendingPayment() {
        List<Payment> pendingPayments = paymentRepository.findAllUnprocessedByPaymentStatus(PaymentStatus.PENDING);

        for(Payment pendingPayment : pendingPayments) {
            try {
                log.debug(">>>>> Schedule [PENDING] payment, paymentId: [{}], transactionId: [{}]",
                        pendingPayment.getPaymentId(), pendingPayment.getTransactionId());

                paymentService.handlePendingPayment(pendingPayment);

                log.debug(">>>>> Success schedule of [PENDING] payment, paymentId: [{}], transactionId: [{}]",
                        pendingPayment.getPaymentId(), pendingPayment.getTransactionId());

            } catch (Exception e) {
                log.debug(">>>>> Error occurred while schedule of [PENDING] payment, paymentId: [{}], transactionId: [{}]. " +
                                "Exception message: {}",
                        pendingPayment.getPaymentId(), pendingPayment.getTransactionId(), e.getMessage());

                onProcessPaymentException(pendingPayment, e);
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