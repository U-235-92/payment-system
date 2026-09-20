package aq.project._utils.entities.payment_provider_service;

import aq.project.dto.PaymentProviderServiceErrorHandleTransactionDto;
import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceSuccessHandleTransactionDto;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequestMetadata;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PaymentProviderServiceEntities {

    public static PaymentProviderServiceTransactionRequest getValidPaymentProviderServiceTransactionRequest() {
        PaymentProviderServiceTransactionRequestMetadata metadata = new PaymentProviderServiceTransactionRequestMetadata();
        metadata.setId(1L);
        metadata.setProcessed(false);
        metadata.setTimestamp(OffsetDateTime.now());
        metadata.setTraceId(UUID.randomUUID().toString());

        PaymentProviderServiceTransactionRequest transactionRequest = new PaymentProviderServiceTransactionRequest();
        transactionRequest.setTransactionId(UUID.randomUUID());
        transactionRequest.setMerchantId(UUID.randomUUID().toString());
        transactionRequest.setOperation(Operation.DEPOSIT);
        transactionRequest.setAmount(BigDecimal.valueOf(100));
        transactionRequest.setCurrencyCode("USD");
        transactionRequest.setNotificationUrl("https://www.example.aq");
        transactionRequest.setTransactionRequestMetadata(metadata);
        return transactionRequest;
    }

    public static PaymentProviderServiceTransactionRequest getInvalidPaymentProviderServiceTransactionRequest() {
        PaymentProviderServiceTransactionRequestMetadata metadata = new PaymentProviderServiceTransactionRequestMetadata();
        metadata.setId(1L);
        metadata.setProcessed(false);
        metadata.setTimestamp(OffsetDateTime.now());
        metadata.setTraceId(null); // null invalid value

        PaymentProviderServiceTransactionRequest transactionRequest = new PaymentProviderServiceTransactionRequest();
        transactionRequest.setTransactionId(UUID.randomUUID());
        transactionRequest.setMerchantId(null); // null invalid value
        transactionRequest.setOperation(Operation.DEPOSIT);
        transactionRequest.setAmount(BigDecimal.valueOf(-100)); // negative amount value
        transactionRequest.setCurrencyCode("TEST"); // invalid currency code
        transactionRequest.setNotificationUrl("https://www.example.aq");
        transactionRequest.setTransactionRequestMetadata(metadata);
        return transactionRequest;
    }

    public static PaymentProviderServiceSuccessHandleTransactionDto getValidPaymentProviderServiceSuccessHandleTransactionDto() {
        PaymentProviderServiceSuccessHandleTransactionDto dto = new PaymentProviderServiceSuccessHandleTransactionDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setMerchantId("merchant-id");
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setNotificationUrl("https://www.example.aq");
        dto.setOperation(Operation.DEPOSIT);
        return dto;
    }

    public static PaymentProviderServiceSuccessHandleTransactionDto getInvalidPaymentProviderServiceSuccessHandleTransactionDto() {
        PaymentProviderServiceSuccessHandleTransactionDto dto = new PaymentProviderServiceSuccessHandleTransactionDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setMerchantId(null);
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setNotificationUrl(null);
        dto.setOperation(Operation.DEPOSIT);
        dto.setDescription("description");
        return dto;
    }

    public static PaymentProviderServiceErrorHandleTransactionDto getValidPaymentProviderServiceErrorHandleTransactionDto() {
        PaymentProviderServiceErrorHandleTransactionDto dto = new PaymentProviderServiceErrorHandleTransactionDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setMerchantId("merchant-id");
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setNotificationUrl("https://www.example.aq");
        dto.setOperation(Operation.DEPOSIT);
        dto.setDescription("description");
        return dto;
    }

    public static PaymentProviderServiceErrorHandleTransactionDto getInvalidPaymentProviderServiceErrorHandleTransactionDto() {
        PaymentProviderServiceErrorHandleTransactionDto dto = new PaymentProviderServiceErrorHandleTransactionDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setMerchantId("merchant-id");
        dto.setTraceId(null);
        dto.setNotificationUrl(null);
        dto.setOperation(Operation.DEPOSIT);
        dto.setDescription("description");
        return dto;
    }
}
