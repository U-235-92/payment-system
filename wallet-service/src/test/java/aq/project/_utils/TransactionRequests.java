package aq.project._utils;

import aq.project.dto.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class TransactionRequests {

// ==================== DepositTransactionRequest ====================

    public static WalletServiceDepositTransactionRequestDto getValidDepositTransactionRequest() {
        WalletServiceDepositTransactionRequestDto request = new WalletServiceDepositTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(100.50));
        request.setCurrencyCode("USD");
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(1.0));
        return request;
    }

    public static WalletServiceDepositTransactionRequestDto getValidDepositTransactionRequest(UUID transactionId, UUID walletId) {
        WalletServiceDepositTransactionRequestDto request = new WalletServiceDepositTransactionRequestDto();
        request.setTransactionId(transactionId);
        request.setWalletId(walletId);
        request.setAmount(BigDecimal.valueOf(100.50));
        request.setCurrencyCode("USD");
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(1.0));
        return request;
    }

    public static WalletServiceDepositTransactionRequestDto getInvalidDepositTransactionRequest() {
        WalletServiceDepositTransactionRequestDto request = new WalletServiceDepositTransactionRequestDto();
//        отсутствует transactionId (null) – нарушает @NotNull
        request.setWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(-100.50));
        request.setCurrencyCode("TEST");
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(1.0));
        return request;
    }

// ==================== TransferTransactionRequest ====================

    public static WalletServiceTransferTransactionRequestDto getValidTransferTransactionRequest() {
        WalletServiceTransferTransactionRequestDto request = new WalletServiceTransferTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setSenderWalletId(UUID.randomUUID());
        request.setRecipientWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(200.00));
        request.setCurrencyCode("EUR");
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setSenderConversionRate(BigDecimal.valueOf(1.2));
        request.setRecipientConversionRate(BigDecimal.valueOf(0.9));
        return request;
    }

    public static WalletServiceTransferTransactionRequestDto getValidTransferTransactionRequest(UUID transactionId, UUID senderWalletId, UUID recipientWalletId) {
        WalletServiceTransferTransactionRequestDto request = new WalletServiceTransferTransactionRequestDto();
        request.setTransactionId(transactionId);
        request.setSenderWalletId(senderWalletId);
        request.setRecipientWalletId(recipientWalletId);
        request.setAmount(BigDecimal.valueOf(200.00));
        request.setCurrencyCode("EUR");
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setSenderConversionRate(BigDecimal.valueOf(1.2));
        request.setRecipientConversionRate(BigDecimal.valueOf(0.9));
        return request;
    }

    public static WalletServiceTransferTransactionRequestDto getInvalidTransferTransactionRequest() {
        WalletServiceTransferTransactionRequestDto request = new WalletServiceTransferTransactionRequestDto();
//        отсутствует senderWalletId (null)
        request.setTransactionId(UUID.randomUUID());
        request.setRecipientWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(-200.00));
        request.setCurrencyCode("usd");
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setSenderConversionRate(BigDecimal.valueOf(-1.2));
        request.setRecipientConversionRate(BigDecimal.valueOf(-0.9));
        return request;
    }

// ==================== WithdrawTransactionRequest ====================

    public static WalletServiceWithdrawTransactionRequestDto getValidWithdrawTransactionRequest() {
        WalletServiceWithdrawTransactionRequestDto request = new WalletServiceWithdrawTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(50.75));
        request.setCurrencyCode("RUB");
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(0.8));
        return request;
    }

    public static WalletServiceWithdrawTransactionRequestDto getValidWithdrawTransactionRequest(UUID transactionId, UUID walletId) {
        WalletServiceWithdrawTransactionRequestDto request = new WalletServiceWithdrawTransactionRequestDto();
        request.setTransactionId(transactionId);
        request.setWalletId(walletId);
        request.setAmount(BigDecimal.valueOf(50.75));
        request.setCurrencyCode("RUB");
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(0.8));
        return request;
    }

    public static WalletServiceWithdrawTransactionRequestDto getInvalidWithdrawTransactionRequest() {
        WalletServiceWithdrawTransactionRequestDto request = new WalletServiceWithdrawTransactionRequestDto();
//        отсутствует walletId (null)
        request.setTransactionId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(50.75));
        request.setCurrencyCode("RUB");
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(null);
        request.setConversionRate(BigDecimal.valueOf(0.8));
        return request;
    }

// ==================== WalletServiceCancelTransactionRequestDto ====================

    public static WalletServiceCancelTransactionRequestDto getValidWalletServiceCancelTransactionRequestDto() {
        WalletServiceCancelTransactionRequestDto request = new WalletServiceCancelTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setTraceId(UUID.randomUUID().toString());
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    public static WalletServiceCancelTransactionRequestDto getInvalidWalletServiceCancelTransactionRequestDto() {
        WalletServiceCancelTransactionRequestDto request = new WalletServiceCancelTransactionRequestDto();
        request.setTransactionId(null);
        request.setTraceId(null);
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    // ==================== WalletServiceFailTransactionRequestDto ====================

    public static WalletServiceFailTransactionRequestDto getValidWalletServiceFailTransactionRequestDto() {
        WalletServiceFailTransactionRequestDto request = new WalletServiceFailTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setTraceId(UUID.randomUUID().toString());
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    public static WalletServiceFailTransactionRequestDto getInvalidWalletServiceFailTransactionRequestDto() {
        WalletServiceFailTransactionRequestDto request = new WalletServiceFailTransactionRequestDto();
        request.setTransactionId(null);
        request.setTraceId(null);
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }
}