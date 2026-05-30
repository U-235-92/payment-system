package aq.project.mocks;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.util.RequestPropertyKeys;
import aq.project.utils.UuidConstants;

import java.util.UUID;

public class TestTransactionEventMocks {

    public static Transaction getValidDepositTransactionEvent() {
        Transaction transaction = new Transaction(
                UuidConstants.STR_TRANSACTION_ID,
                OperationType.DEPOSIT,
                TransactionStatus.PENDING,
                System.currentTimeMillis(),
                false
        );
        transaction.putProperty(RequestPropertyKeys.RECIPIENT_PERSON_ID, UuidConstants.STR_PERSON_ID);
        transaction.putProperty(RequestPropertyKeys.RECIPIENT_WALLET_ID, UuidConstants.STR_WALLET_ID);
        return transaction;
    }

    public static Transaction getInvalidDepositTransactionEvent() {
        Transaction transaction = new Transaction(
                "abc",
                OperationType.DEPOSIT,
                TransactionStatus.PENDING,
                -1L,
                false
        );
        transaction.putProperty(RequestPropertyKeys.RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        transaction.putProperty(RequestPropertyKeys.RECIPIENT_WALLET_ID, UuidConstants.STR_WALLET_ID);
        return transaction;
    }

    public static Transaction getDepositTransactionEventWithUnknownUuid() {
        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                OperationType.DEPOSIT,
                TransactionStatus.PENDING,
                System.currentTimeMillis(),
                false
        );
        transaction.putProperty(RequestPropertyKeys.RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        transaction.putProperty(RequestPropertyKeys.RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        return transaction;
    }
}
