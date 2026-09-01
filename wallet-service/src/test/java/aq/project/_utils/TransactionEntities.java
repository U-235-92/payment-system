package aq.project._utils;

import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.entities.transaction.TransactionMetadata;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class TransactionEntities {

// -------------------- TransactionMetadata --------------------

    public static TransactionMetadata getValidTransactionMetadata() {
        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setTraceId(UUID.randomUUID().toString());
        metadata.setTimestamp(OffsetDateTime.now());
        return metadata;
    }

    public static TransactionMetadata getInvalidTransactionMetadata() {
        TransactionMetadata metadata = new TransactionMetadata();
//        отсутствует traceId (null) – нарушает @NotNull
        metadata.setTimestamp(OffsetDateTime.now());
        return metadata;
    }

// -------------------- DepositTransaction --------------------

    public static DepositTransaction getValidDepositTransaction() {
        DepositTransaction tx = new DepositTransaction();
        tx.setId(UUID.randomUUID());
        tx.setWalletId(UUID.randomUUID());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setProcessed(false);
        tx.setMetadata(getValidTransactionMetadata());
        return tx;
    }

    public static DepositTransaction getValidDepositTransaction(UUID id) {
        DepositTransaction tx = new DepositTransaction();
        tx.setId(id);
        tx.setWalletId(UUID.randomUUID());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setProcessed(false);
        tx.setMetadata(getValidTransactionMetadata());
        return tx;
    }

    public static DepositTransaction getInvalidDepositTransaction() {
        DepositTransaction tx = new DepositTransaction();
//        отсутствует id (null) – нарушает @NotNull
        tx.setWalletId(UUID.randomUUID());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setProcessed(false);
        tx.setMetadata(getValidTransactionMetadata());
        return tx;
    }

// -------------------- WithdrawTransaction --------------------

    public static WithdrawTransaction getValidWithdrawTransaction() {
        WithdrawTransaction tx = new WithdrawTransaction();
        tx.setId(UUID.randomUUID());
        tx.setWalletId(UUID.randomUUID());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setProcessed(false);
        tx.setMetadata(getValidTransactionMetadata());
        return tx;
    }

    public static WithdrawTransaction getValidWithdrawTransaction(UUID id) {
        WithdrawTransaction tx = new WithdrawTransaction();
        tx.setId(id);
        tx.setWalletId(UUID.randomUUID());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setProcessed(false);
        tx.setMetadata(getValidTransactionMetadata());
        return tx;
    }

    public static WithdrawTransaction getInvalidWithdrawTransaction() {
        WithdrawTransaction tx = new WithdrawTransaction();
        tx.setId(UUID.randomUUID());
//        отсутствует walletId (null)
        tx.setStatus(TransactionStatus.PENDING);
        tx.setProcessed(false);
        tx.setMetadata(getValidTransactionMetadata());
        return tx;
    }

// -------------------- TransferTransaction --------------------

    public static TransferTransaction getValidTransferTransaction() {
        TransferTransaction tx = new TransferTransaction();
        tx.setId(UUID.randomUUID());
        tx.setSenderWalletId(UUID.randomUUID());
        tx.setRecipientWalletId(UUID.randomUUID());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setProcessed(false);
        tx.setMetadata(getValidTransactionMetadata());
        return tx;
    }

    public static TransferTransaction getValidTransferTransaction(UUID id) {
        TransferTransaction tx = new TransferTransaction();
        tx.setId(id);
        tx.setSenderWalletId(UUID.randomUUID());
        tx.setRecipientWalletId(UUID.randomUUID());
        tx.setStatus(TransactionStatus.PENDING);
        tx.setProcessed(false);
        tx.setMetadata(getValidTransactionMetadata());
        return tx;
    }

    public static TransferTransaction getInvalidTransferTransaction() {
        TransferTransaction tx = new TransferTransaction();
        tx.setId(UUID.randomUUID());
        tx.setSenderWalletId(UUID.randomUUID());
//        отсутствует recipientWalletId (null)
        tx.setStatus(TransactionStatus.PENDING);
        tx.setProcessed(false);
        tx.setMetadata(getValidTransactionMetadata());
        return tx;
    }

// -------------------- Дополнительные методы --------------------

    public static DepositTransaction getValidDepositTransactionWithStatus(TransactionStatus status) {
        DepositTransaction tx = getValidDepositTransaction();
        tx.setStatus(status);
        return tx;
    }

    public static WithdrawTransaction getValidWithdrawTransactionWithStatus(TransactionStatus status) {
        WithdrawTransaction tx = getValidWithdrawTransaction();
        tx.setStatus(status);
        return tx;
    }

    public static TransferTransaction getValidTransferTransactionWithStatus(TransactionStatus status) {
        TransferTransaction tx = getValidTransferTransaction();
        tx.setStatus(status);
        return tx;
    }
}