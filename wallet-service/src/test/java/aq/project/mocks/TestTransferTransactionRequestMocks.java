package aq.project.mocks;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.messages.TransactionRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.util.UUID;

import static aq.project.util.RequestPropertyKeys.*;
import static aq.project.utils.UuidConstants.*;

public class TestTransferTransactionRequestMocks {

    public static TransactionRequest getValidTransferMessageRequest() {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.TRANSFER,
                new BigDecimal("15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, STR_WALLET_ID);
        transactionRequest.putProperty(SENDER_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(SENDER_WALLET_ID, STR_WALLET_ID);
        return transactionRequest;
    }

    public static TransactionRequest getValidTransferMessageRequest(String recipientWalletId, String recipientPersonId, String senderWalletId, String senderPersonId) {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.TRANSFER,
                new BigDecimal("15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, recipientPersonId);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, recipientWalletId);
        transactionRequest.putProperty(SENDER_PERSON_ID, senderPersonId);
        transactionRequest.putProperty(SENDER_WALLET_ID, senderWalletId);
        return transactionRequest;
    }

    public static TransactionRequest getInvalidTransferMessageRequest() {
        TransactionRequest transactionRequest = new TransactionRequest(
                "abc",
                OperationType.TRANSFER,
                new BigDecimal("15.588"),
                "RUB",
                TransactionStatus.PENDING,
                -1L
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        transactionRequest.putProperty(SENDER_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(SENDER_WALLET_ID, STR_WALLET_ID);
        return transactionRequest;
    }

    public static TransactionRequest getTransferMessageRequestWithUnknownUuid() {
        TransactionRequest transactionRequest = new TransactionRequest(
                UUID.randomUUID().toString(),
                OperationType.TRANSFER,
                new BigDecimal("15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        transactionRequest.putProperty(SENDER_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(SENDER_WALLET_ID, STR_WALLET_ID);
        return transactionRequest;
    }

    public static TransactionRequest getInvalidTransferMessageRequestWithNegativeAmount() {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.TRANSFER,
                new BigDecimal("-15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, STR_WALLET_ID);
        transactionRequest.putProperty(SENDER_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(SENDER_WALLET_ID, STR_WALLET_ID);
        return transactionRequest;
    }

    public static TransactionRequest getValidTransferMessageRequestWithBigAmount(String recipientWalletId, String recipientPersonId, String senderWalletId, String senderPersonId) {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.TRANSFER,
                new BigDecimal("8585858585.85"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, recipientPersonId);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, recipientWalletId);
        transactionRequest.putProperty(SENDER_PERSON_ID, senderPersonId);
        transactionRequest.putProperty(SENDER_WALLET_ID, senderWalletId);
        return transactionRequest;
    }

    public static ConsumerRecord<String, TransactionRequest> getValidTransferConsumerRecord(String recipientWalletId, String recipientPersonId, String senderWalletId, String senderPersonId) {
        TransactionRequest request = getValidTransferMessageRequest(recipientWalletId, recipientPersonId, senderWalletId, senderPersonId);
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(SENDER_WALLET_ID, getPropertyBytes(request, SENDER_WALLET_ID));
        consumerRecord.headers().add(SENDER_PERSON_ID, getPropertyBytes(request, SENDER_PERSON_ID));
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getValidTransferConsumerRecord() {
        TransactionRequest request = getValidTransferMessageRequest();
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(SENDER_WALLET_ID, getPropertyBytes(request, SENDER_WALLET_ID));
        consumerRecord.headers().add(SENDER_PERSON_ID, getPropertyBytes(request, SENDER_PERSON_ID));
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getInvalidTransferConsumerRecord() {
        TransactionRequest request = getInvalidTransferMessageRequest();
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(SENDER_WALLET_ID, getPropertyBytes(request, SENDER_WALLET_ID));
        consumerRecord.headers().add(SENDER_PERSON_ID, getPropertyBytes(request, SENDER_PERSON_ID));
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getTransferConsumerRecordWithUnknownUuid() {
        TransactionRequest request = getTransferMessageRequestWithUnknownUuid();
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(SENDER_WALLET_ID, getPropertyBytes(request, SENDER_WALLET_ID));
        consumerRecord.headers().add(SENDER_PERSON_ID, getPropertyBytes(request, SENDER_PERSON_ID));
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getTransferConsumerRecordWithNegativeAmount() {
        TransactionRequest request = getInvalidTransferMessageRequestWithNegativeAmount();
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(SENDER_WALLET_ID, getPropertyBytes(request, SENDER_WALLET_ID));
        consumerRecord.headers().add(SENDER_PERSON_ID, getPropertyBytes(request, SENDER_PERSON_ID));
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getTransferConsumerRecordWithBigAmount(String recipientWalletId, String recipientPersonId, String senderWalletId, String senderPersonId) {
        TransactionRequest request = getValidTransferMessageRequestWithBigAmount(recipientWalletId, recipientPersonId, senderWalletId, senderPersonId);
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(SENDER_WALLET_ID, getPropertyBytes(request, SENDER_WALLET_ID));
        consumerRecord.headers().add(SENDER_PERSON_ID, getPropertyBytes(request, SENDER_PERSON_ID));
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        return consumerRecord;
    }

    private static byte[] getPropertyBytes(TransactionRequest transactionRequest, String key) {
        return transactionRequest.getProperty(key, String.class).getBytes();
    }
}
