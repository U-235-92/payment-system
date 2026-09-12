package aq.project._utils;

import aq.project.dto.TransactionStatus;
import aq.project.dto.DepositTransactionRequestWalletServiceDto;
import aq.project.dto.TransferTransactionRequestWalletServiceDto;
import aq.project.dto.WithdrawTransactionRequestWalletServiceDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class TransactionRequests {

// ==================== DepositTransactionRequest ====================

    public static DepositTransactionRequestWalletServiceDto getValidDepositTransactionRequest() {
        DepositTransactionRequestWalletServiceDto request = new DepositTransactionRequestWalletServiceDto();
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

    public static DepositTransactionRequestWalletServiceDto getValidDepositTransactionRequest(UUID transactionId, UUID walletId) {
        DepositTransactionRequestWalletServiceDto request = new DepositTransactionRequestWalletServiceDto();
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

    public static DepositTransactionRequestWalletServiceDto getInvalidDepositTransactionRequest() {
        DepositTransactionRequestWalletServiceDto request = new DepositTransactionRequestWalletServiceDto();
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

    public static TransferTransactionRequestWalletServiceDto getValidTransferTransactionRequest() {
        TransferTransactionRequestWalletServiceDto request = new TransferTransactionRequestWalletServiceDto();
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

    public static TransferTransactionRequestWalletServiceDto getValidTransferTransactionRequest(UUID transactionId, UUID senderWalletId, UUID recipientWalletId) {
        TransferTransactionRequestWalletServiceDto request = new TransferTransactionRequestWalletServiceDto();
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

    public static TransferTransactionRequestWalletServiceDto getInvalidTransferTransactionRequest() {
        TransferTransactionRequestWalletServiceDto request = new TransferTransactionRequestWalletServiceDto();
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

    public static WithdrawTransactionRequestWalletServiceDto getValidWithdrawTransactionRequest() {
        WithdrawTransactionRequestWalletServiceDto request = new WithdrawTransactionRequestWalletServiceDto();
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

    public static WithdrawTransactionRequestWalletServiceDto getValidWithdrawTransactionRequest(UUID transactionId, UUID walletId) {
        WithdrawTransactionRequestWalletServiceDto request = new WithdrawTransactionRequestWalletServiceDto();
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

    public static WithdrawTransactionRequestWalletServiceDto getInvalidWithdrawTransactionRequest() {
        WithdrawTransactionRequestWalletServiceDto request = new WithdrawTransactionRequestWalletServiceDto();
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

    public static DepositTransactionRequestWalletServiceDto getValidDepositTransactionRequestWithStatus(TransactionStatus status) {
        DepositTransactionRequestWalletServiceDto request = getValidDepositTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }

    public static TransferTransactionRequestWalletServiceDto getValidTransferTransactionRequestWithStatus(TransactionStatus status) {
        TransferTransactionRequestWalletServiceDto request = getValidTransferTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }

    public static WithdrawTransactionRequestWalletServiceDto getValidWithdrawTransactionRequestWithStatus(TransactionStatus status) {
        WithdrawTransactionRequestWalletServiceDto request = getValidWithdrawTransactionRequest();
        request.setTransactionStatus(status);
        return request;
    }
}