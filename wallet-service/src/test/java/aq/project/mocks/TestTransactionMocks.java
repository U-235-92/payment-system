package aq.project.mocks;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.dto.OperationType;

import java.util.UUID;

public class TestTransactionMocks {

    public static Transaction getValidTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setProcessed(false);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        return transaction;
    }
}
