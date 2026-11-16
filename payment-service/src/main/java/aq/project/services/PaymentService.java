package aq.project.services;

import aq.project.clients.IndividualsApiServiceRestClient;
import aq.project.dto.*;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.fake_payment_provider_service.PayoutApiClient;
import aq.project.mappers.PaymentMapper;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.jwt.JwtUtil;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.TraceContext;
import aq.project.utils.telemetry.TraceFactory;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher applicationEventPublisher;

    private final PaymentRepository paymentRepository;

    private final PaymentMethodRepository paymentMethodRepository;

    private final PayoutApiClient payoutApiClient;

    private final IndividualsApiServiceRestClient individualsApiServiceRestClient;

    private final AuthorizationService authorizationService;

    private final TokenService tokenService;

    private final TraceContext traceContext;

    private final Fallback fallback;

    @RateLimiter(
            name = "create-payment-rate-limiter",
            fallbackMethod = "createPaymentRateLimiterFallback"
    )
    @Transactional
    public PaymentServiceCreatePaymentResponseDto createPayment(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto
    ) {
        Long paymentMethodId = paymentServiceCreatePaymentRequestDto.getPaymentMethodId();

        PaymentMethod paymentMethod = getPaymentMethod(paymentMethodId);

        Payment newPayment = paymentMapper.toPayment(paymentServiceCreatePaymentRequestDto);

        newPayment.setPaymentId(UUID.randomUUID());
        newPayment.setStatus(PaymentStatus.PENDING);
        newPayment.setProcessed(false);
        newPayment.setPaymentMethod(paymentMethod);

        Payment savedPayment = paymentRepository.save(newPayment);

//        Continue to async handle saved payment
//        See PaymentEventListener.onCommitPayment(..) for details
        applicationEventPublisher.publishEvent(savedPayment);

        return paymentMapper.toPaymentServiceCreatePaymentResponseDto(savedPayment);
    }

    private PaymentServiceCreatePaymentResponseDto createPaymentRateLimiterFallback(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto,
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
    public List<PaymentServicePaymentMethodResponseDto> getPaymentMethods(
            String currencyCode,
            String countryCode
    ) {
        return paymentMethodRepository
                .findAllByCurrencyCodeAndCountryCode(currencyCode, countryCode)
                .stream()
                .map(paymentMapper::toPaymentServicePaymentMethodResponseDto)
                .toList();
    }

    private List<PaymentServicePaymentMethodResponseDto> getPaymentMethodsRateLimiterFallback(
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

        ResponseEntity<PayoutInfoResponseDto> responseEntity = payoutApiClient
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

    public void handleProcessingPaymentFallbackRetry(
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
            name = "handle-complete-payment-retry",
            fallbackMethod = "handleCompletePaymentFallbackRetry"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCompletePayment(
            Payment payment
    ) {
//        Confirm complete of transaction by sending request for update its status into individuals-api-service
//        payment-service -> individuals-api-service -> transaction-service
        UUID transactionId = payment.getTransactionId();

        String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());
        String traceId = traceContext.getTraceId() == null ? TraceFactory.genTraceId() : traceContext.getTraceId();

        individualsApiServiceRestClient.completeTransaction(transactionId, authorization, traceId);

        payment.markProcessed();

        paymentRepository.save(payment);
    }

    private void handleCompletePaymentFallbackRetry(
            Payment payment,
            Exception exception
    ) {
        String action = "handle-complete-payment";

        String message = String.format(
                "Exception occurred during handle of payment in status: [%s] for transaction with id: [%s]. " +
                        "Exception message: %s",
                payment.getStatus(), payment.getTransactionId(), exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);
    }

    @Retry(
            name = "handle-fail-payment-retry",
            fallbackMethod = "handleFailPaymentFallbackRetry"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailPayment(
            Payment payment
    ) {
//        Confirm fail of transaction by sending request for update its status into individuals-api-service
//        payment-service -> individuals-api-service -> transaction-service
        UUID transactionId = payment.getTransactionId();

        String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());
        String traceId = traceContext.getTraceId() == null ? TraceFactory.genTraceId() : traceContext.getTraceId();

        individualsApiServiceRestClient.failTransaction(transactionId, authorization, traceId);

        payment.markProcessed();

        paymentRepository.save(payment);
    }

    private void handleFailPaymentFallbackRetry(
            Payment payment,
            Exception exception
    ) {
        String action = "handle-fail-payment";

        String message = String.format(
                "Exception occurred during handle of payment in status: [%s] for transaction with id: [%s]. " +
                        "Exception message: %s",
                payment.getStatus(), payment.getTransactionId(), exception.getMessage());

        fallback.handleRetryFallback(action, message, exception);
    }
}