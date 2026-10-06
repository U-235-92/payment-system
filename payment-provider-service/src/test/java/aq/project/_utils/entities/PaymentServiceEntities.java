package aq.project._utils.entities;

import aq.project.dto.*;
import aq.project.entities.Merchant;
import aq.project.entities.Payment;
import aq.project.entities.Transaction;
import aq.project.entities.TransactionMetadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PaymentServiceEntities {

    public static PaymentProviderServiceCreatePaymentRequestDto getValidPaymentProviderServiceCreatePaymentRequestDto() {
        PaymentProviderServiceCreatePaymentRequestDto dto = new PaymentProviderServiceCreatePaymentRequestDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setPaymentMethodId(UUID.randomUUID());
        dto.setTraceId(UUID.randomUUID().toString());
        return dto;
    }

    public static PaymentProviderServiceCreatePaymentRequestDto getInvalidPaymentProviderServiceCreatePaymentRequestDto() {
        PaymentProviderServiceCreatePaymentRequestDto dto = new PaymentProviderServiceCreatePaymentRequestDto();
        dto.setTransactionId(null);
        dto.setPaymentMethodId(UUID.randomUUID());
        dto.setTraceId(UUID.randomUUID().toString());
        return dto;
    }

    public static PaymentProviderServiceFailPaymentRequestDto getValidPaymentProviderServiceFailPaymentRequestDto() {
        PaymentProviderServiceFailPaymentRequestDto dto = new PaymentProviderServiceFailPaymentRequestDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setPaymentId(UUID.randomUUID());
        dto.setTraceId(UUID.randomUUID().toString());
        return dto;
    }

    public static PaymentProviderServiceFailPaymentRequestDto getInvalidPaymentProviderServiceFailPaymentRequestDto() {
        PaymentProviderServiceFailPaymentRequestDto dto = new PaymentProviderServiceFailPaymentRequestDto();
        dto.setTransactionId(null);
        dto.setPaymentId(null);
        dto.setTraceId(UUID.randomUUID().toString());
        return dto;
    }

    public static Transaction getValidTransaction() {
        TransactionMetadata transactionMetadata = new TransactionMetadata();
        transactionMetadata.setCreatedAt(OffsetDateTime.now());
        transactionMetadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setMerchant(getValidMerchant());
        transaction.setAmount(BigDecimal.valueOf(58.85));
        transaction.setCurrencyCode("USD");
        transaction.setOperation(Operation.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription("description");
        transaction.setNotificationUrl("https://www.example.aq");
        transaction.setMetadata(transactionMetadata);
        return transaction;
    }

    public static Transaction getInvalidTransaction() {
        TransactionMetadata transactionMetadata = new TransactionMetadata();
        transactionMetadata.setCreatedAt(OffsetDateTime.now());
        transactionMetadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(null);
        transaction.setMerchant(getValidMerchant());
        transaction.setAmount(BigDecimal.valueOf(-58.85));
        transaction.setCurrencyCode("TEST");
        transaction.setOperation(Operation.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription("description");
        transaction.setNotificationUrl("https://www.example.aq");
        transaction.setMetadata(transactionMetadata);
        return transaction;
    }

    public static Merchant getValidMerchant() {
        Merchant merchant = new Merchant();
        merchant.setId(UUID.randomUUID().toString());
        merchant.setSecretKey(UUID.randomUUID().toString());
        merchant.setName("Merchant");
        merchant.setCreatedAt(OffsetDateTime.now());
        merchant.setUpdatedAt(OffsetDateTime.now());
        merchant.setRole(UserRole.USER);
        return merchant;
    }

    public static Payment getValidPayment() {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setTransaction(getValidTransaction());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setUpdatedAt(OffsetDateTime.now());
        return payment;
    }

    public static Payment getInvalidPayment() {
        Payment payment = new Payment();
        payment.setId(null);
        payment.setTransaction(getValidTransaction());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setUpdatedAt(OffsetDateTime.now());
        return payment;
    }
}
