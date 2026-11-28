package aq.project.services;

import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.entities.Payment;
import aq.project.fake_payment_provider_service.PayoutApiClient;
import aq.project.mappers.PaymentMapper;
import aq.project.repositories.PaymentRepository;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.TraceFactory;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutService {

    private final PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    private final PayoutApiClient payoutClient;

    private final PaymentRepository paymentRepository;

    private final AuthorizationService authorizationService;

    private final Fallback fallback;

    @Retry(
            name = "handle-pending-payout-retry",
            fallbackMethod = "handlePendingPayoutPaymentRetryFallback"
    )
    @Transactional
    public void handlePendingPayoutPayment(
            Payment payment
    ) {
        String traceId = TraceFactory.genTraceId();
        String authorization = authorizationService.getAuthorization();
        String merchantId = authorizationService.getMerchantId();

        CreatePayoutRequestDto createPayoutRequestDto = paymentMapper
                .toCreatePayoutRequestDto(payment);

        createPayoutRequestDto.setMerchantId(merchantId);

        log.debug(">>>>> Attempt to send [payout request] into [fake-payment-provider-service]");

        ResponseEntity<PayoutInfoResponseDto> responseEntity = payoutClient
                .createPayout(authorization, createPayoutRequestDto, traceId);

        HttpStatusCode httpStatusCode = responseEntity.getStatusCode();

        log.debug(">>>>> Received response on [payout request] from [fake-payment-provider-service] " +
                "with status code: {}", httpStatusCode);

        if(httpStatusCode.is2xxSuccessful()) {
//            The final process of payment to success/fail state will continue on WebhookEventKafkaConsumer side
//            See WebhookEventKafkaConsumer.onWebhookEvent(...) for details
            PayoutInfoResponseDto payoutInfoResponseDto = responseEntity.getBody();

            log.debug(">>>>> Received response on [payout request] from [fake-payment-provider-service] " +
                    "with body: {}", payoutInfoResponseDto);

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

    private void handlePendingPayoutPaymentRetryFallback(
            Payment payment,
            Exception exception
    ) throws Exception {
        log.debug(">>>>> Fallback call. Exception details: {}. Message: {}",
                exception.getClass().getSimpleName(), exception.getMessage());

        if(exception instanceof HttpClientErrorException
                || exception instanceof IllegalStateException)
            throw exception;

        String action = "handle-pending-payout";

        PaymentStatus paymentStatus = payment.getStatus();

        UUID transactionId = payment.getTransactionId();

        String exceptionClassName = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();

        String message = String.format(
                "%s occurred during handle of [%s] payout for transaction with id: [%s]. " +
                        "Exception message: %s",
                exceptionClassName, paymentStatus, transactionId, exceptionMessage);

        fallback.handleRetryFallback(action, message, exception);
    }
}