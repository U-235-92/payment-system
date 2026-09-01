package aq.project._utils;

import aq.project.dto.TransactionStatus;
import aq.project.messages.requests.DepositTransactionRequest;
import aq.project.messages.requests.TransferTransactionRequest;
import aq.project.messages.requests.WithdrawTransactionRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class TransactionRequests {

// ==================== DepositTransactionRequest ====================

    public static DepositTransactionRequest getValidDepositTransactionRequest() {
        DepositTransactionRequest request = new DepositTransactionRequest();
        request.setTransactionId(UUID.randomUUID());
        request.setWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(100.50));
        request.setCurrency("USD");
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(1.0));
        return request;
    }

    public static DepositTransactionRequest getValidDepositTransactionRequest(UUID transactionId, UUID walletId) {
        DepositTransactionRequest request = new DepositTransactionRequest();
        request.setTransactionId(transactionId);
        request.setWalletId(walletId);
        request.setAmount(BigDecimal.valueOf(100.50));
        request.setCurrency("USD");
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(1.0));
        return request;
    }

    public static DepositTransactionRequest getInvalidDepositTransactionRequest() {
        DepositTransactionRequest request = new DepositTransactionRequest();
//        отсутствует transactionId (null) – нарушает @NotNull
        request.setWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(-100.50));
        request.setCurrency("TEST");
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(1.0));
        return request;
    }

// ==================== TransferTransactionRequest ====================

    public static TransferTransactionRequest getValidTransferTransactionRequest() {
        TransferTransactionRequest request = new TransferTransactionRequest();
        request.setTransactionId(UUID.randomUUID());
        request.setSenderWalletId(UUID.randomUUID());
        request.setRecipientWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(200.00));
        request.setCurrency("EUR");
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setSenderConversionRate(BigDecimal.valueOf(1.2));
        request.setRecipientConversionRate(BigDecimal.valueOf(0.9));
        return request;
    }

    public static TransferTransactionRequest getValidTransferTransactionRequest(UUID transactionId, UUID senderWalletId, UUID recipientWalletId) {
        TransferTransactionRequest request = new TransferTransactionRequest();
        request.setTransactionId(transactionId);
        request.setSenderWalletId(senderWalletId);
        request.setRecipientWalletId(recipientWalletId);
        request.setAmount(BigDecimal.valueOf(200.00));
        request.setCurrency("EUR");
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setSenderConversionRate(BigDecimal.valueOf(1.2));
        request.setRecipientConversionRate(BigDecimal.valueOf(0.9));
        return request;
    }

    public static TransferTransactionRequest getInvalidTransferTransactionRequest() {
        TransferTransactionRequest request = new TransferTransactionRequest();
//        отсутствует senderWalletId (null)
        request.setTransactionId(UUID.randomUUID());
        request.setRecipientWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(-200.00));
        request.setCurrency("usd");
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setSenderConversionRate(BigDecimal.valueOf(-1.2));
        request.setRecipientConversionRate(BigDecimal.valueOf(-0.9));
        return request;
    }

// ==================== WithdrawTransactionRequest ====================

    public static WithdrawTransactionRequest getValidWithdrawTransactionRequest() {
        WithdrawTransactionRequest request = new WithdrawTransactionRequest();
        request.setTransactionId(UUID.randomUUID());
        request.setWalletId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(50.75));
        request.setCurrency("RUB");
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(0.8));
        return request;
    }

    public static WithdrawTransactionRequest getValidWithdrawTransactionRequest(UUID transactionId, UUID walletId) {
        WithdrawTransactionRequest request = new WithdrawTransactionRequest();
        request.setTransactionId(transactionId);
        request.setWalletId(walletId);
        request.setAmount(BigDecimal.valueOf(50.75));
        request.setCurrency("RUB");
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(UUID.randomUUID().toString());
        request.setConversionRate(BigDecimal.valueOf(0.8));
        return request;
    }

    public static WithdrawTransactionRequest getInvalidWithdrawTransactionRequest() {
        WithdrawTransactionRequest request = new WithdrawTransactionRequest();
//        отсутствует walletId (null)
        request.setTransactionId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(50.75));
        request.setCurrency("RUB");
        request.setTransactionStatus(TransactionStatus.PENDING);
        request.setTimestamp(OffsetDateTime.now());
        request.setTraceId(null);
        request.setConversionRate(BigDecimal.valueOf(0.8));
        return request;
    }

// ==================== Дополнительные методы для удобства ====================

    public static DepositTransactionRequest getValidDepositTransactionRequestWithStatus(TransactionStatus status) {
        DepositTransactionRequest request = getValidDepositTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }

    public static TransferTransactionRequest getValidTransferTransactionRequestWithStatus(TransactionStatus status) {
        TransferTransactionRequest request = getValidTransferTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }

    public static WithdrawTransactionRequest getValidWithdrawTransactionRequestWithStatus(TransactionStatus status) {
        WithdrawTransactionRequest request = getValidWithdrawTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }
}