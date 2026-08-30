package aq.project._utils;

import aq.project.dto.MerchantRegistrationRequestDto;
import aq.project.dto.Operation;
import aq.project.dto.TransactionStatus;
import aq.project.dto.UserRole;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.entities.TransactionMetadata;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

public class Entities {

    public static Transaction getValidPendingTransaction() {
        TransactionMetadata transactionMetadata = new TransactionMetadata();
        transactionMetadata.setCreatedAt(OffsetDateTime.now());
        transactionMetadata.setUpdatedAt(OffsetDateTime.now());
        transactionMetadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.fromString(getValidTransactionId()));
        transaction.setMerchant(getValidMerchant());
        transaction.setAmount(BigDecimal.valueOf(58.85));
        transaction.setCurrency("USD");
        transaction.setOperation(Operation.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription("description");
        transaction.setNotificationUrl("http://example:8080/notification");
        transaction.setMetadata(transactionMetadata);
        return transaction;
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

    public static String getBase64BasicAuthorizationValue() {
        String keyPass = String.format("%s:%s",getValidMerchantId(), getValidSecret());
        return String.format("%s%s", "Basic ", Base64.getEncoder().encodeToString(keyPass.getBytes()));
    }

    public static String getValidTransactionId() {
        return "0134f097-c27b-430b-81e7-d33b07f154c4";
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

    public static String getInvalidTransactionId() {
        return "wrong_id";
    }
}
