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
        request.setTransactionStatus(TransactionStatus.PENDING);
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
        request.setTransactionStatus(TransactionStatus.PENDING);
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
        request.setTransactionStatus(TransactionStatus.PENDING);
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
        request.setTransactionStatus(TransactionStatus.PENDING);
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
        request.setTransactionStatus(TransactionStatus.PENDING);
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
        request.setTransactionStatus(TransactionStatus.PENDING);
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
        request.setTransactionStatus(TransactionStatus.PENDING);
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
        request.setTransactionStatus(TransactionStatus.PENDING);
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
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(null);
        request.setConversionRate(BigDecimal.valueOf(0.8));
        return request;
    }

// ==================== WalletServiceCancelDepositTransactionRequestDto ====================

    public static WalletServiceCancelDepositTransactionRequestDto getValidWalletServiceCancelDepositTransactionRequestDto() {
        WalletServiceCancelDepositTransactionRequestDto request = new WalletServiceCancelDepositTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setTraceId(UUID.randomUUID().toString());
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    public static WalletServiceCancelDepositTransactionRequestDto getInvalidWalletServiceCancelDepositTransactionRequestDto() {
        WalletServiceCancelDepositTransactionRequestDto request = new WalletServiceCancelDepositTransactionRequestDto();
        request.setTransactionId(null);
        request.setTraceId(null);
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

// ==================== WalletServiceCancelWithdrawTransactionRequestDto ====================

    public static WalletServiceCancelWithdrawTransactionRequestDto getValidWalletServiceCancelWithdrawTransactionRequestDto() {
        WalletServiceCancelWithdrawTransactionRequestDto request = new WalletServiceCancelWithdrawTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setTraceId(UUID.randomUUID().toString());
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    public static WalletServiceCancelWithdrawTransactionRequestDto getInvalidWalletServiceCancelWithdrawTransactionRequestDto() {
        WalletServiceCancelWithdrawTransactionRequestDto request = new WalletServiceCancelWithdrawTransactionRequestDto();
        request.setTransactionId(null);
        request.setTraceId(null);
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

// ==================== WalletServiceCancelTransferTransactionRequestDto ====================

    public static WalletServiceCancelTransferTransactionRequestDto getValidWalletServiceCancelTransferTransactionRequestDto() {
        WalletServiceCancelTransferTransactionRequestDto request = new WalletServiceCancelTransferTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setTraceId(UUID.randomUUID().toString());
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    public static WalletServiceCancelTransferTransactionRequestDto getInvalidWalletServiceCancelTransferTransactionRequestDto() {
        WalletServiceCancelTransferTransactionRequestDto request = new WalletServiceCancelTransferTransactionRequestDto();
        request.setTransactionId(null);
        request.setTraceId(null);
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    // ==================== WalletServiceFailDepositTransactionRequestDto ====================

    public static WalletServiceFailDepositTransactionRequestDto getValidWalletServiceFailDepositTransactionRequestDto() {
        WalletServiceFailDepositTransactionRequestDto request = new WalletServiceFailDepositTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setTraceId(UUID.randomUUID().toString());
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    public static WalletServiceFailDepositTransactionRequestDto getInvalidWalletServiceFailDepositTransactionRequestDto() {
        WalletServiceFailDepositTransactionRequestDto request = new WalletServiceFailDepositTransactionRequestDto();
        request.setTransactionId(null);
        request.setTraceId(null);
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

// ==================== WalletServiceFailWithdrawTransactionRequestDto ====================

    public static WalletServiceFailWithdrawTransactionRequestDto getValidWalletServiceFailWithdrawTransactionRequestDto() {
        WalletServiceFailWithdrawTransactionRequestDto request = new WalletServiceFailWithdrawTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setTraceId(UUID.randomUUID().toString());
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    public static WalletServiceFailWithdrawTransactionRequestDto getInvalidWalletServiceFailWithdrawTransactionRequestDto() {
        WalletServiceFailWithdrawTransactionRequestDto request = new WalletServiceFailWithdrawTransactionRequestDto();
        request.setTransactionId(null);
        request.setTraceId(null);
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

// ==================== WalletServiceFailTransferTransactionRequestDto ====================

    public static WalletServiceFailTransferTransactionRequestDto getValidWalletServiceFailTransferTransactionRequestDto() {
        WalletServiceFailTransferTransactionRequestDto request = new WalletServiceFailTransferTransactionRequestDto();
        request.setTransactionId(UUID.randomUUID());
        request.setTraceId(UUID.randomUUID().toString());
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

    public static WalletServiceFailTransferTransactionRequestDto getInvalidWalletServiceFailTransferTransactionRequestDto() {
        WalletServiceFailTransferTransactionRequestDto request = new WalletServiceFailTransferTransactionRequestDto();
        request.setTransactionId(null);
        request.setTraceId(null);
        request.setTimestamp(OffsetDateTime.now());
        return request;
    }

// ==================== Дополнительные методы для удобства ====================

    public static WalletServiceDepositTransactionRequestDto getValidDepositTransactionRequestWithStatus(TransactionStatus status) {
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }

    public static WalletServiceTransferTransactionRequestDto getValidTransferTransactionRequestWithStatus(TransactionStatus status) {
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }

    public static WalletServiceWithdrawTransactionRequestDto getValidWithdrawTransactionRequestWithStatus(TransactionStatus status) {
        WalletServiceWithdrawTransactionRequestDto request = getValidWithdrawTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }
}