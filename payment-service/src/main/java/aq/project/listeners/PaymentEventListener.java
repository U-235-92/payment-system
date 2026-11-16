package aq.project.listeners;

import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.entities.Payment;
import aq.project.fake_payment_provider_service.PayoutApiClient;
import aq.project.mappers.PaymentMapper;
import aq.project.repositories.PaymentRepository;
import aq.project.services.AuthorizationService;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.TraceFactory;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    private final PaymentRepository paymentRepository;

    private final PayoutApiClient payoutApiClient;

    private final AuthorizationService authorizationService;

    private final Fallback fallback;

    @Async
    @Retry(
            name = "on-commit-payment-retry",
            fallbackMethod = "onCommitPaymentRetryFallback"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommitPayment(
            Payment payment
    ) {
        PaymentStatus paymentStatus = payment.getStatus();

        if(!payment.isProcessed() && paymentStatus == PaymentStatus.PENDING)
            handlePendingPayment(payment);
    }

    private void onCommitPaymentRetryFallback(
            Payment payment,
            Exception exception
    ) {
        String action = "handle-payment-commit-event";

        String message = String.format(
                "Exception occurred during handle of payment commit event for transaction with id: [%s]. " +
                        "Exception message: %s",
                payment.getTransactionId(), exception.getMessage());

        payment.fail();

        paymentRepository.save(payment);

        try {
            fallback.handleRetryFallback(action, message, exception);
        } catch (Exception e) {
//            Suppressed to commit payment fail
        }
    }

    private void handlePendingPayment(Payment payment) {
        String traceId = TraceFactory.genTraceId();
        String authorization = authorizationService.getAuthorization();
        String merchantId = authorizationService.getMerchantId();

        CreatePayoutRequestDto createPayoutRequestDto = paymentMapper.toCreatePayoutRequestDto(payment);

        createPayoutRequestDto.setMerchantId(merchantId);

        ResponseEntity<PayoutInfoResponseDto> responseEntity = payoutApiClient
                .createPayout(authorization, createPayoutRequestDto, traceId);

        HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

        if(httpStatusCode.is2xxSuccessful()) {
//            The final process of payment to success/fail state will continue on PaymentScheduler side
//            See PaymentScheduler.scheduleProcessingPayment(..) for details
            PayoutInfoResponseDto payoutInfoResponseDto = responseEntity.getBody();

            if(payoutInfoResponseDto != null) {
                payment.setExternalId(payoutInfoResponseDto.getId());
                payment.processing();

                paymentRepository.save(payment);
            } else {
                throw new IllegalStateException("Received response has no body");
            }
        }
    }
}