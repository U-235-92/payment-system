package aq.project._utils;

import aq.project.dto.TransactionStatus;
import aq.project.dto.DepositTransactionRequestDto;
import aq.project.dto.TransferTransactionRequestDto;
import aq.project.dto.WithdrawTransactionRequestDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class TransactionRequests {

// ==================== DepositTransactionRequest ====================

    public static DepositTransactionRequestDto getValidDepositTransactionRequest() {
        DepositTransactionRequestDto request = new DepositTransactionRequestDto();
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

    public static DepositTransactionRequestDto getValidDepositTransactionRequest(UUID transactionId, UUID walletId) {
        DepositTransactionRequestDto request = new DepositTransactionRequestDto();
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

    public static DepositTransactionRequestDto getInvalidDepositTransactionRequest() {
        DepositTransactionRequestDto request = new DepositTransactionRequestDto();
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

    public static TransferTransactionRequestDto getValidTransferTransactionRequest() {
        TransferTransactionRequestDto request = new TransferTransactionRequestDto();
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

    public static TransferTransactionRequestDto getValidTransferTransactionRequest(UUID transactionId, UUID senderWalletId, UUID recipientWalletId) {
        TransferTransactionRequestDto request = new TransferTransactionRequestDto();
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

    public static TransferTransactionRequestDto getInvalidTransferTransactionRequest() {
        TransferTransactionRequestDto request = new TransferTransactionRequestDto();
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

    public static WithdrawTransactionRequestDto getValidWithdrawTransactionRequest() {
        WithdrawTransactionRequestDto request = new WithdrawTransactionRequestDto();
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

    public static WithdrawTransactionRequestDto getValidWithdrawTransactionRequest(UUID transactionId, UUID walletId) {
        WithdrawTransactionRequestDto request = new WithdrawTransactionRequestDto();
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

    public static WithdrawTransactionRequestDto getInvalidWithdrawTransactionRequest() {
        WithdrawTransactionRequestDto request = new WithdrawTransactionRequestDto();
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

// ==================== Дополнительные методы для удобства ====================

    public static DepositTransactionRequestDto getValidDepositTransactionRequestWithStatus(TransactionStatus status) {
        DepositTransactionRequestDto request = getValidDepositTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }

    public static TransferTransactionRequestDto getValidTransferTransactionRequestWithStatus(TransactionStatus status) {
        TransferTransactionRequestDto request = getValidTransferTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }

    public static WithdrawTransactionRequestDto getValidWithdrawTransactionRequestWithStatus(TransactionStatus status) {
        WithdrawTransactionRequestDto request = getValidWithdrawTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }
}