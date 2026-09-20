package aq.project._utils.entities.transaction_service;

import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceTransactionMetadata;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WithdrawTransactionServiceEntities {

    public static TransactionServiceWithdrawTransaction getValidTransactionServiceWithdrawTransaction() {
        TransactionServiceTransactionMetadata metadata = new TransactionServiceTransactionMetadata();
        metadata.setId(1L);
        metadata.setTimestamp(OffsetDateTime.now());
        metadata.setTraceId(UUID.randomUUID().toString());

        TransactionServiceWithdrawTransaction transaction = new TransactionServiceWithdrawTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(UUID.randomUUID());
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCurrencyCode("USD");
        transaction.setConversionRate(BigDecimal.valueOf(1.00));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setNotificationUrl("https://www.example.com");
        transaction.setTransactionMetadata(metadata);
        return transaction;
    }

    public static TransactionServiceWithdrawTransaction getInvalidTransactionServiceWithdrawTransaction() {
        TransactionServiceTransactionMetadata metadata = new TransactionServiceTransactionMetadata();
        metadata.setId(1L);
        metadata.setTimestamp(OffsetDateTime.now());
        metadata.setTraceId(null); // null value

        TransactionServiceWithdrawTransaction transaction = new TransactionServiceWithdrawTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWalletId(UUID.randomUUID());
        transaction.setAmount(BigDecimal.valueOf(-100)); // negative amount value
        transaction.setCurrencyCode("TEST"); // incorrect currency code
        transaction.setConversionRate(BigDecimal.valueOf(1.00));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setNotificationUrl("https://www.example.com");
        transaction.setTransactionMetadata(metadata);
        return transaction;
    }
}
