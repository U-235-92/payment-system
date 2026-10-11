package aq.project.utils.handlers;

import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.exceptions.ProcessOperationException;
import aq.project.payment_provider_service.PaymentApiClient;
import aq.project.utils.mappers.PaymentMapper;
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

    public PaymentProviderServiceCreatePaymentResponseDto createPayment(
            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto
    ) {
        String authorization = getAuthorization();
        String traceId = paymentServiceCreatePaymentRequestDto.getTraceId();

        UUID transactionId = paymentServiceCreatePaymentRequestDto.getTransactionId();

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = paymentMapper
                .toPaymentProviderServiceCreatePaymentRequestDto(paymentServiceCreatePaymentRequestDto);

        ResponseEntity<PaymentProviderServiceCreatePaymentResponseDto> response = paymentApiClient
                .createPayment(authorization, paymentProviderServiceCreatePaymentRequestDto, traceId);

        PaymentProviderServiceCreatePaymentResponseDto paymentResponseDto = response.getBody();

        if(paymentResponseDto == null)
            throw new ProcessOperationException(String.format(
                    "Fail while creating payment for transaction with id: [%s] on [%s] side: " +
                    "received response is null", transactionId, paymentProviderServiceName));

        return paymentResponseDto;
    }

    private String getAuthorization() {
        String keyPass = String.format("%s:%s", merchantId, merchantSecret);
        String base64KeyPass = Base64.getEncoder().encodeToString(keyPass.getBytes());
        return String.format("Basic %s", base64KeyPass);
    }
}
