package aq.project._utils;

import aq.project.dto.*;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import static aq.project.utils.constants.RequestPropertyKeys.TRANSACTION_ID;
import static aq.project.utils.constants.RequestPropertyKeys.TRANSACTION_STATUS;

public class Entities {

    public static Transaction getValidTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.fromString(getValidTransactionId()));
        transaction.setMerchant(getValidMerchant());
        transaction.setAmount(BigDecimal.valueOf(58.85));
        transaction.setCurrency("USD");
        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(OffsetDateTime.now());
        transaction.setUpdatedAt(OffsetDateTime.now());
        return transaction;
    }

    public static String getValidTransactionId() {
        return "0134f097-c27b-430b-81e7-d33b07f154c4";
    }

    public static Merchant getValidMerchant() {
        Merchant merchant = new Merchant();
        merchant.setId(getValidMerchantId());
        merchant.setSecretKey(getValidSecret());
        merchant.setName("test-merchant-service");
        merchant.setCreatedAt(OffsetDateTime.now());
        merchant.setUpdatedAt(OffsetDateTime.now());
        merchant.setRole(UserRole.USER);
        return merchant;
    }

    public static String getBase64BasicAuthorizationValue() {
        String keyPass = String.format("%s:%s",getValidMerchantId(), getValidSecret());
        return String.format("%s%s", "Basic ", Base64.getEncoder().encodeToString(keyPass.getBytes()));
    }

    public static String getValidMerchantId() {
        return "test-merchant-service";
    }

    public static String getValidSecret() {
        return "super_secret";
    }

    public static String getInvalidMerchantId() {
        return "";
    }

    public static TransactionRequestDto getInvalidTransactionRequestDto() {
        TransactionRequestDto dto = new TransactionRequestDto();
        dto.getProperties().put(TRANSACTION_ID, getInvalidTransactionId());
        dto.getProperties().put(TRANSACTION_STATUS, TransactionStatus.PENDING.getValue());
        dto.setAmount("-58.85");
        dto.setCurrency("HELLO");
        dto.setOperationType(OperationType.DEPOSIT);
        return dto;
    }

    public static String getInvalidTransactionId() {
        return "wrong_id";
    }

    public static TransactionRequestDto getValidTransactionRequestDto() {
        TransactionRequestDto dto = new TransactionRequestDto();
        dto.getProperties().put(TRANSACTION_ID, UUID.randomUUID().toString());
        dto.getProperties().put(TRANSACTION_STATUS, TransactionStatus.PENDING.getValue());
        dto.setAmount("58.85");
        dto.setCurrency("USD");
        dto.setOperationType(OperationType.DEPOSIT);
        return dto;
    }

    public static TransactionStatusDto getValidTransactionStatusDto() {
        TransactionStatusDto dto = new TransactionStatusDto();
        dto.setId(getValidTransactionId());
        dto.setStatus(TransactionStatus.COMPLETED);
        dto.setEventType(EventType.TRANSACTION_COMPLETED);
        dto.setNotificationUrl("https://www.example.com");
        dto.setDescription("This is the description");
        return dto;
    }

    public static TransactionStatusDto getInvalidTransactionStatusDto() {
        TransactionStatusDto dto = new TransactionStatusDto();
        dto.setId(getInvalidTransactionId());
        dto.setStatus(TransactionStatus.COMPLETED);
        dto.setEventType(EventType.TRANSACTION_COMPLETED);
        dto.setNotificationUrl("https://www.example.com");
        dto.setDescription("This is the description");
        return dto;
    }

    public static MerchantRegistrationRequestDto getValidUserMerchantRegistrationRequestDto() {
        MerchantRegistrationRequestDto dto = new MerchantRegistrationRequestDto();
        dto.setMerchantId(getValidMerchantId());
        dto.setSecretKey(getValidSecret());
        dto.setName("test-merchant-service");
        dto.setRole(UserRole.USER);
        return dto;
    }

    public static MerchantRegistrationRequestDto getInvalidUserMerchantRegistrationRequestDto() {
        MerchantRegistrationRequestDto dto = new MerchantRegistrationRequestDto();
        dto.setMerchantId(getInvalidMerchantId());
        dto.setSecretKey(getValidSecret());
        dto.setName("test-merchant-service");
        dto.setRole(UserRole.USER);
        return dto;
    }

    public static CancelTransactionDto getValidCancelTransactionDto() {
        CancelTransactionDto dto = new CancelTransactionDto();
        dto.setId(getValidTransactionId());
        dto.setStatus(TransactionStatus.FAILED);
        return dto;
    }

    public static CancelTransactionDto getInvalidCancelTransactionDto() {
        CancelTransactionDto dto = new CancelTransactionDto();
        dto.setId(getInvalidTransactionId());
        dto.setStatus(TransactionStatus.FAILED);
        return dto;
    }
}
