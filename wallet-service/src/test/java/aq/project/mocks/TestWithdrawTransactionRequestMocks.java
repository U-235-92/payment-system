package aq.project.mocks;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.messages.TransactionRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;

import static aq.project.utils.UuidConstants.*;
import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static aq.project.utils.constants.RequestPropertyKeys.*;

public class TestWithdrawTransactionRequestMocks {

    public static TransactionRequest getValidWithdrawMessageRequest() {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.WITHDRAW,
                new BigDecimal("15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, STR_WALLET_ID);
        return transactionRequest;
    }

    public static TransactionRequest getValidWithdrawMessageRequest(String walletId, String personId) {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.WITHDRAW,
                new BigDecimal("15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, personId);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, walletId);
        transactionRequest.putProperty(RECIPIENT_CURRENCY_RATE, BigDecimal.valueOf(1.0085).toString());
        return transactionRequest;
    }

    public static TransactionRequest getInvalidWithdrawMessageRequest() {
        TransactionRequest transactionRequest = new TransactionRequest(
                "abc",
                OperationType.WITHDRAW,
                new BigDecimal("15.588"),
                "RUB",
                TransactionStatus.PENDING,
                -1L
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        return transactionRequest;
    }

    public static TransactionRequest getWithdrawMessageRequestWithUnknownUuid() {
        TransactionRequest transactionRequest = new TransactionRequest(
                UUID.randomUUID().toString(),
                OperationType.WITHDRAW,
                new BigDecimal("15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        return transactionRequest;
    }

    public static TransactionRequest getInvalidWithdrawMessageRequestWithNegativeAmount() {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.WITHDRAW,
                new BigDecimal("-15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, STR_WALLET_ID);
        return transactionRequest;
    }

    public static TransactionRequest getValidWithdrawMessageRequestWithBigAmount(String walletId, String personId) {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.WITHDRAW,
                new BigDecimal("8585858585.85"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, personId);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, walletId);
        return transactionRequest;
    }

    public static ConsumerRecord<String, TransactionRequest> getValidWithdrawConsumerRecord(String walletId, String personId) {
        TransactionRequest request = getValidWithdrawMessageRequest(walletId, personId);
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        consumerRecord.headers().add(RECIPIENT_CURRENCY_RATE, getPropertyBytes(request, RECIPIENT_CURRENCY_RATE));
        consumerRecord.headers().add(X_TRACE_ID_HEADER, getDefaultTraceId().getBytes());
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getValidWithdrawConsumerRecord() {
        TransactionRequest request = getValidWithdrawMessageRequest();
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        consumerRecord.headers().add(X_TRACE_ID_HEADER, getDefaultTraceId().getBytes());
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getInvalidWithdrawConsumerRecordWithNoTraceIdHeader(String walletId, String personId) {
        TransactionRequest request = getValidWithdrawMessageRequest(walletId, personId);
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getInvalidWithdrawConsumerRecord() {
        TransactionRequest request = getInvalidWithdrawMessageRequest();
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getWithdrawConsumerRecordWithUnknownUuid() {
        TransactionRequest request = getWithdrawMessageRequestWithUnknownUuid();
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        consumerRecord.headers().add(X_TRACE_ID_HEADER, getDefaultTraceId().getBytes());
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getWithdrawConsumerRecordWithNegativeAmount() {
        TransactionRequest request = getInvalidWithdrawMessageRequestWithNegativeAmount();
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        return consumerRecord;
    }

    public static ConsumerRecord<String, TransactionRequest> getWithdrawConsumerRecordWithBigAmount(String walletId, String personId) {
        TransactionRequest request = getValidWithdrawMessageRequestWithBigAmount(walletId, personId);
        ConsumerRecord<String, TransactionRequest> consumerRecord = new ConsumerRecord<>(
                "wallet_operation_request",
                0,
                0L,
                null,
                request);
        consumerRecord.headers().add(RECIPIENT_WALLET_ID, getPropertyBytes(request, RECIPIENT_WALLET_ID));
        consumerRecord.headers().add(RECIPIENT_PERSON_ID, getPropertyBytes(request, RECIPIENT_PERSON_ID));
        consumerRecord.headers().add(X_TRACE_ID_HEADER, getDefaultTraceId().getBytes());
        return consumerRecord;
    }

    private static byte[] getPropertyBytes(TransactionRequest transactionRequest, String key) {
        return transactionRequest.getProperty(key).getBytes();
    }

    private static String getDefaultTraceId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder traceId = new StringBuilder();
        for(byte b : bytes) {
            traceId.append(String.format("%02x", b));
        }
        return traceId.toString();
    }
}
