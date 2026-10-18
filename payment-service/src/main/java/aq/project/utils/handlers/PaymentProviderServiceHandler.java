package aq.project.utils.handlers;

import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentProviderServiceFailPaymentRequestDto;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.payment_provider_service.PaymentApiClient;
import aq.project.utils.mappers.PaymentMapper;
import aq.project.utils.resilence.Fallback;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentProviderServiceHandler {

    @Value("${service.payment-service.merchant-id}")
    private String merchantId;
    @Value("${service.payment-service.merchant-secret}")
    private String merchantSecret;
    @Value("${service.payment-provider-service.name}")
    private String paymentProviderServiceName;

    private final PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    private final PaymentApiClient paymentApiClient;

    private final Fallback fallback;

    public PaymentProviderServiceCreatePaymentResponseDto createPayment(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto
    ) {
        String authorization = getAuthorization();
        String traceId = paymentServiceCreatePaymentRequestDto.getTraceId();

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = paymentMapper
                .toPaymentProviderServiceCreatePaymentRequestDto(paymentServiceCreatePaymentRequestDto);
        paymentProviderServiceCreatePaymentRequestDto.setMerchantId(merchantId);

        ResponseEntity<PaymentProviderServiceCreatePaymentResponseDto> response = paymentApiClient
                .createPayment(authorization, paymentProviderServiceCreatePaymentRequestDto, traceId);

        return response.getBody();
    }

    @Retry(
            name = "fail-payment-retry",
            fallbackMethod = "failPaymentFallbackRetry"
    )
    @CircuitBreaker(
            name = "fail-payment-circuitbreaker",
            fallbackMethod = "failPaymentFallbackCircuitBreaker"
    )
    public void failPayment(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto
    ) {
        PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto = paymentMapper
                .toPaymentProviderServiceFailPaymentRequestDto(paymentServiceCreatePaymentRequestDto);

        paymentProviderServiceFailPaymentRequestDto.setMerchantId(merchantId);

        String authorization = getAuthorization();
        String traceId = paymentServiceCreatePaymentRequestDto.getTraceId();

        paymentApiClient.failPayment(authorization, paymentProviderServiceFailPaymentRequestDto, traceId);
    }

    private void failPaymentFallbackRetry(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto,
            Exception exception
    ) {
        UUID transactionId = paymentServiceCreatePaymentRequestDto.getTransactionId();

        String action = "fail-payment";
        String message = String.format(
                "Exception occurred during fail of payment for transaction with id: [%s]",
                transactionId);

        fallback.handleRetryFallback(action, message, exception);
    }

    private void failPaymentFallbackCircuitBreaker(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto,
            Exception exception
    ) {
        UUID transactionId = paymentServiceCreatePaymentRequestDto.getTransactionId();

        String action = "fail-payment";
        String message = String.format(
                "Exception occurred during fail of payment for transaction with id: [%s]",
                transactionId);

        fallback.handleBulkheadFallback(action, message, exception);
    }

    private String getAuthorization() {
        String keyPass = String.format("%s:%s", merchantId, merchantSecret);
        String base64KeyPass = Base64.getEncoder().encodeToString(keyPass.getBytes());
        return String.format("Basic %s", base64KeyPass);
    }
}
