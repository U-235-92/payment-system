package aq.project.services;

import aq.project.dto.*;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.mappers.PaymentMapper;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    private final PaymentRepository paymentRepository;

    private final PaymentMethodRepository paymentMethodRepository;

    private final TransactionService transactionService;

    private final PayoutService payoutService;

    private final Fallback fallback;

    @RateLimiter(
            name = "create-payment-rate-limiter",
            fallbackMethod = "createPaymentRateLimiterFallback"
    )
    @Transactional
    public CreatePaymentResponseDto createPayment(
            CreatePaymentRequestDto createPaymentRequestDto
    ) {
        Long paymentMethodId = createPaymentRequestDto.getPaymentMethodId();

        log.debug(">>>>> Received request to create payment with payment method id: {}", paymentMethodId);

        PaymentMethod paymentMethod = getPaymentMethod(paymentMethodId);

        Payment newPayment = paymentMapper.toPayment(createPaymentRequestDto);

        newPayment.setPaymentId(UUID.randomUUID());
        newPayment.setStatus(PaymentStatus.PENDING);
        newPayment.setProcessed(false);
        newPayment.setPaymentMethod(paymentMethod);

//        Continue to async handle saved payment.
//        See PaymentScheduler.schedulePendingPayment(..) for details.
        Payment savedPayment = paymentRepository.save(newPayment);

        log.debug(">>>>> Success handle of request to create payment with payment method id: {}", paymentMethodId);

        return paymentMapper.toCreatePaymentResponseDto(savedPayment);
    }

    private CreatePaymentResponseDto createPaymentRateLimiterFallback(
            CreatePaymentRequestDto paymentServiceCreatePaymentRequestDto,
            RequestNotPermitted exception
    ) {
        String action = "create-payment";

        String message = String.format(
                "Exception occurred during creating a payment for transaction with id: [%s]. " +
                        "Exception message: %s",
                paymentServiceCreatePaymentRequestDto.getTransactionId(), exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }

    private PaymentMethod getPaymentMethod(Long paymentMethodId) {
        return paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Payment method with id: [%s] not found", paymentMethodId)));
    }

    @RateLimiter(
            name = "get-payment-methods-rate-limiter",
            fallbackMethod = "getPaymentMethodsRateLimiterFallback"
    )
    public List<PaymentMethodResponseDto> getPaymentMethods(
            String currencyCode,
            String countryCode
    ) {
        return paymentMethodRepository
                .findAllByCurrencyCodeAndCountryCode(currencyCode, countryCode)
                .stream()
                .map(paymentMapper::toPaymentMethodResponseDto)
                .toList();
    }

    private List<PaymentMethodResponseDto> getPaymentMethodsRateLimiterFallback(
            String currencyCode,
            String countryCode,
            RequestNotPermitted exception
    ) {
        String action = "get-payment-methods";

        String message = String.format(
                "Exception occurred during getting payment methods for currency: [%s] and country: [%s]. " +
                        "Exception message: %s",
                currencyCode, countryCode, exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);

        return null;
    }

    @Retry(
            name = "handle-processing-payment-retry",
            fallbackMethod = "handleProcessingPaymentFallbackRetry"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProcessingPayment(
            Payment payment
    ) {
        TransactionType transactionType = payment.getTransactionType();

        switch (transactionType) {
            case DEPOSIT -> transactionService.handleProcessingTransactionPayment(payment);

            case WITHDRAWAL -> payoutService.handleProcessingPayoutPayment(payment);
        }
    }

    private void handleProcessingPaymentFallbackRetry(
            Payment payment,
            Exception exception
    ) {
        String action = "handle-processed-payment";

        String message = String.format(
                "Exception occurred during handle of payment in status: [%s] for transaction with id: [%s]. " +
                        "Exception message: %s",
                payment.getStatus(), payment.getTransactionId(), exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);
    }

    @Retry(
            name = "handle-pending-payment-retry",
            fallbackMethod = "handlePendingPaymentRetryFallback"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePendingPayment(
            Payment payment
    ) {
        TransactionType operationType = payment.getTransactionType();

        switch (operationType) {
            case DEPOSIT -> transactionService.handlePendingTransactionPayment(payment);

            case WITHDRAWAL -> payoutService.handlePendingPayoutPayment(payment);
        }
    }

    private void handlePendingPaymentRetryFallback(
            Payment payment,
            Exception exception
    ) {
        String action = "handle-pending-payment";

        PaymentStatus paymentStatus = payment.getStatus();

        UUID transactionId = payment.getTransactionId();

        String exceptionClassName = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during handle of [%s] payment for transaction with id: [%s]. " +
                        "Exception message: %s",
                exceptionClassName, paymentStatus, transactionId, exceptionMessage);

//        In case if fake-payment-provider-service is not accessed then mark payment as failed.
        payment.fail();

//        Finalize FAIL status of transaction on other services in transaction flow.
//        See PaymentScheduler.scheduleFailPayment(..) for details.
        paymentRepository.save(payment);

        try {
            fallback.handleRetryFallback(action, message, exception);
        } catch (Exception ex) {
//            Exception was suppressed to commit payment status without errors.
        }
    }
}