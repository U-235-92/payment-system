package aq.project.mocks;

import aq.project.dto.TransactionStatus;
import aq.project.entities.OutboxEvent;
import aq.project.dto.OperationType;

import java.util.UUID;

public class TestOutboxEventMocks {

    public static OutboxEvent getValidOutboxEvent() {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setTransactionId(UUID.randomUUID().toString());
        outboxEvent.setOperationType(OperationType.DEPOSIT);
        outboxEvent.setProcessed(false);
        outboxEvent.setTimestamp(System.currentTimeMillis());
        outboxEvent.setTransactionStatus(TransactionStatus.PENDING);
        return outboxEvent;
    }
}
