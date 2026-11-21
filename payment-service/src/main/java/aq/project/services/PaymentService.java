package aq.project.services;

import aq.project.dto.*;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.fake_payment_provider_service.PayoutApiClient;
import aq.project.fake_payment_provider_service.TransactionApiClient;
import aq.project.mappers.PaymentMapper;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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

    private final PayoutApiClient payoutClient;

    private final TransactionApiClient transactionClient;

    private final AuthorizationService authorizationService;

    private final TraceContext traceContext;

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

        PaymentMethod paymentMethod = getPaymentMethod(paymentMethodId);

        Payment newPayment = paymentMapper.toPayment(createPaymentRequestDto);

        newPayment.setPaymentId(UUID.randomUUID());
        newPayment.setStatus(PaymentStatus.PENDING);
        newPayment.setProcessed(false);
        newPayment.setPaymentMethod(paymentMethod);

//        Continue to async handle saved payment.
//        See PaymentScheduler.schedulePendingPayment(..) for details.
        Payment savedPayment = paymentRepository.save(newPayment);

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
        Long payoutId = payment.getExternalId();

        String authorization = authorizationService.getAuthorization();
        String traceId = traceContext.getTraceId() == null ? TraceFactory.genTraceId() : traceContext.getTraceId();

        ResponseEntity<PayoutInfoResponseDto> responseEntity = payoutClient
                .getPayoutInfo(payoutId, authorization, traceId);

        HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

        if(httpStatusCode.is2xxSuccessful()) {
            PayoutInfoResponseDto payoutInfoResponseDto = responseEntity.getBody();

            if(payoutInfoResponseDto != null) {
                Status payoutStatus = payoutInfoResponseDto.getStatus();

                switch (payoutStatus) {
                    case SUCCESS -> {
//                        The final process of payment by sending result of operation into individuals-api-service
//                        See PaymentScheduler.scheduleCompletePayment(..) for details
                        payment.complete();
                        paymentRepository.save(payment);
                    }

                    case FAIL -> {
//                        The final process of payment by sending result of operation into individuals-api-service
//                        See PaymentScheduler.scheduleFailPayment(..) for details
                        payment.fail();
                        paymentRepository.save(payment);

                    }
                }
            } else {
                log.error("[{}]: Received response has no body", traceId);
            }
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
        String traceId = TraceFactory.genTraceId();
        String authorization = authorizationService.getAuthorization();
        String merchantId = authorizationService.getMerchantId();

        TransactionType operationType = payment.getTransactionType();

        switch (operationType) {
            case DEPOSIT -> {
                CreateTransactionRequestDto createTransactionRequestDto = paymentMapper.toCreateTransactionRequestDto(payment);

                ResponseEntity<TransactionInfoResponseDto> responseEntity = transactionClient
                        .createTransaction(authorization, createTransactionRequestDto, traceId);

                HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

                if(httpStatusCode.is2xxSuccessful()) {
//            The final process of payment to success/fail state will continue on PaymentStatusKafkaConsumer side
//            See PaymentStatusKafkaConsumer.onPaymentStatusEvent(..) for details
                    TransactionInfoResponseDto transactionInfoResponseDto = responseEntity.getBody();

                    if(transactionInfoResponseDto != null) {
                        Long transactionId = transactionInfoResponseDto.getId();

                        payment.setExternalId(transactionId);
                        payment.processing();

                        paymentRepository.save(payment);
                    } else {
                        throw new IllegalStateException("Received response has no body");
                    }
                }
            }

            case WITHDRAWAL -> {
                CreatePayoutRequestDto createPayoutRequestDto = paymentMapper.toCreatePayoutRequestDto(payment);

                createPayoutRequestDto.setMerchantId(merchantId);

                ResponseEntity<PayoutInfoResponseDto> responseEntity = payoutClient
                        .createPayout(authorization, createPayoutRequestDto, traceId);

                HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

                if(httpStatusCode.is2xxSuccessful()) {
//            The final process of payment to success/fail state will continue on PaymentStatusKafkaConsumer side
//            See PaymentStatusKafkaConsumer.onPaymentStatusEvent(..) for details
                    PayoutInfoResponseDto payoutInfoResponseDto = responseEntity.getBody();

                    if(payoutInfoResponseDto != null) {
                        Long payoutId = payoutInfoResponseDto.getId();

                        payment.setExternalId(payoutId);
                        payment.processing();

                        paymentRepository.save(payment);
                    } else {
                        throw new IllegalStateException("Received response has no body");
                    }
                }
            }
        }
    }

    private void handlePendingPaymentRetryFallback(
            Payment payment,
            Exception exception
    ) {
        String action = "handle-pending-payment";

        String message = String.format(
                "Exception occurred during handle of [%s] payment for transaction with id: [%s]. " +
                        "Exception message: %s",
                payment.getStatus(), payment.getTransactionId(), exception.getMessage());

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