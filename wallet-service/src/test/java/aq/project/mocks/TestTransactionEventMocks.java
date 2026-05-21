package aq.project.mocks;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.entities.OutboxEvent;
import aq.project.util.RequestPropertyKeys;
import aq.project.utils.UuidConstants;

import java.util.UUID;

public class TestTransactionEventMocks {

    public static OutboxEvent getValidDepositTransactionEvent() {
        OutboxEvent outboxEvent = new OutboxEvent(
                UuidConstants.STR_TRANSACTION_ID,
                OperationType.DEPOSIT,
                TransactionStatus.PENDING,
                System.currentTimeMillis(),
                false
        );
        outboxEvent.putProperty(RequestPropertyKeys.RECIPIENT_PERSON_ID, UuidConstants.STR_PERSON_ID);
        outboxEvent.putProperty(RequestPropertyKeys.RECIPIENT_WALLET_ID, UuidConstants.STR_WALLET_ID);
        return outboxEvent;
    }

    public static OutboxEvent getInvalidDepositTransactionEvent() {
        OutboxEvent outboxEvent = new OutboxEvent(
                "abc",
                OperationType.DEPOSIT,
                TransactionStatus.PENDING,
                -1L,
                false
        );
        outboxEvent.putProperty(RequestPropertyKeys.RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        outboxEvent.putProperty(RequestPropertyKeys.RECIPIENT_WALLET_ID, UuidConstants.STR_WALLET_ID);
        return outboxEvent;
    }

    public static OutboxEvent getDepositTransactionEventWithUnknownUuid() {
        OutboxEvent outboxEvent = new OutboxEvent(
                UUID.randomUUID().toString(),
                OperationType.DEPOSIT,
                TransactionStatus.PENDING,
                System.currentTimeMillis(),
                false
        );
        outboxEvent.putProperty(RequestPropertyKeys.RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        outboxEvent.putProperty(RequestPropertyKeys.RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        return outboxEvent;
    }
}
