package aq.project._utils.entities.transaction_service;

import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceTransactionMetadata;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TransferTransactionServiceEntities {

    public static TransactionServiceTransferTransaction getValidTransactionServiceTransferTransaction() {
        TransactionServiceTransactionMetadata metadata = new TransactionServiceTransactionMetadata();
        metadata.setId(1L);
        metadata.setTimestamp(OffsetDateTime.now());
        metadata.setTraceId(UUID.randomUUID().toString());

        TransactionServiceTransferTransaction transaction = new TransactionServiceTransferTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setSenderWalletId(UUID.randomUUID());
        transaction.setRecipientWalletId(UUID.randomUUID());
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCurrencyCode("USD");
        transaction.setSenderConversionRate(BigDecimal.valueOf(1.00));
        transaction.setRecipientConversionRate(BigDecimal.valueOf(1.00));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setNotificationUrl("https://www.example.com");
        transaction.setTransactionMetadata(metadata);
        return transaction;
    }

    public static TransactionServiceTransferTransaction getInvalidTransactionServiceTransferTransaction() {
        TransactionServiceTransactionMetadata metadata = new TransactionServiceTransactionMetadata();
        metadata.setId(1L);
        metadata.setTimestamp(OffsetDateTime.now());
        metadata.setTraceId(null); // null value

        TransactionServiceTransferTransaction transaction = new TransactionServiceTransferTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setSenderWalletId(UUID.randomUUID());
        transaction.setRecipientWalletId(UUID.randomUUID());
        transaction.setAmount(BigDecimal.valueOf(-100)); // negative amount value
        transaction.setCurrencyCode("TEST"); // incorrect currency code
        transaction.setSenderConversionRate(BigDecimal.valueOf(-1.00)); // incorrect conversion rate
        transaction.setRecipientConversionRate(BigDecimal.valueOf(1.00));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setNotificationUrl("https://www.example.com");
        transaction.setTransactionMetadata(metadata);
        return transaction;
    }
}
