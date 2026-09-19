package aq.project._utils.entities;

import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceCancelTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.dto.UserRole;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.entities.TransactionMetadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class CancelTransactionHandlerEntities {

    public static PaymentProviderServiceCancelTransactionRequestDto getValidPaymentProviderServiceCancelTransactionRequestDto() {
        PaymentProviderServiceCancelTransactionRequestDto dto = new PaymentProviderServiceCancelTransactionRequestDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setMerchantId(UUID.randomUUID().toString());
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setNotificationUrl("https://www.example.aq");
        dto.setOperation(Operation.DEPOSIT);
        return dto;
    }

    public static PaymentProviderServiceCancelTransactionRequestDto getInvalidPaymentProviderServiceCancelTransactionRequestDto() {
        PaymentProviderServiceCancelTransactionRequestDto dto = new PaymentProviderServiceCancelTransactionRequestDto();
        dto.setTransactionId(null);
        dto.setMerchantId(UUID.randomUUID().toString());
        dto.setTraceId(null);
        dto.setNotificationUrl("https://www.example.aq");
        dto.setOperation(Operation.DEPOSIT);
        return dto;
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

    public static Merchant getInvalidMerchant() {
        Merchant merchant = new Merchant();
        merchant.setId(null);
        merchant.setSecretKey(UUID.randomUUID().toString());
        merchant.setName(null);
        merchant.setCreatedAt(OffsetDateTime.now());
        merchant.setUpdatedAt(OffsetDateTime.now());
        merchant.setRole(UserRole.USER);
        return merchant;
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
}
