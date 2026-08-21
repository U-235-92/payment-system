package aq.project._utils;

import aq.project.dto.CancelTransactionDto;
import aq.project.dto.OperationType;
import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.entities.TransactionMetadata;
import aq.project.messages.TransactionRequest;
import aq.project.messages.TransactionResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static aq.project._utils.Constants.STR_PERSON_ID;
import static aq.project._utils.Constants.STR_WALLET_ID;
import static aq.project.utils.constants.RequestPropertyKeys.*;

public abstract class Entities {

    public static Transaction getValidPendingNotProcessedTransaction() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);

        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setRetryCount(0);
        metadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(85.58));
        transaction.setCurrency("USD");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setProcessed(false);
        transaction.setMetadata(metadata);
        transaction.setProperties(properties);

        return transaction;
    }

    public static Transaction getValidPendingNotProcessedTransaction(String transactionId) {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);

        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setRetryCount(0);
        metadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(85.58));
        transaction.setCurrency("USD");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setProcessed(false);
        transaction.setMetadata(metadata);
        transaction.setProperties(properties);

        return transaction;
    }

    public static Transaction getInvalidPendingNotProcessedTransaction() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);

        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setRetryCount(0);
        metadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(-85.58));
        transaction.setCurrency("TEST");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setProcessed(false);
        transaction.setMetadata(metadata);
        transaction.setProperties(properties);

        return transaction;
    }

    public static Transaction getValidCompletedNotProcessedTransaction() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);

        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setRetryCount(0);
        metadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(85.58));
        transaction.setCurrency("USD");
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setProcessed(false);
        transaction.setMetadata(metadata);
        transaction.setProperties(properties);

        return transaction;
    }

    public static Transaction getValidCompletedProcessedTransaction() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);

        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setRetryCount(0);
        metadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(85.58));
        transaction.setCurrency("USD");
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setProcessed(true);
        transaction.setMetadata(metadata);
        transaction.setProperties(properties);

        return transaction;
    }

    public static Transaction getValidFailedNotProcessedTransaction() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);

        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setRetryCount(0);
        metadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(85.58));
        transaction.setCurrency("USD");
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setProcessed(false);
        transaction.setMetadata(metadata);
        transaction.setProperties(properties);

        return transaction;
    }

    public static Transaction getValidFailedProcessedTransaction() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);

        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setRetryCount(0);
        metadata.setTraceId(UUID.randomUUID().toString());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setOperationType(OperationType.DEPOSIT);
        transaction.setAmount(BigDecimal.valueOf(85.58));
        transaction.setCurrency("USD");
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setProcessed(true);
        transaction.setMetadata(metadata);
        transaction.setProperties(properties);

        return transaction;
    }


    public static ConsumerRecord<String, TransactionResponse> getValidCompletedConsumerRecord() {
        String key = "key";
        TransactionResponse value = new TransactionResponse();
        value.setTransactionId(UUID.randomUUID().toString());
        value.setOperationType(OperationType.DEPOSIT);
        value.setTransactionStatus(TransactionStatus.COMPLETED);
        value.setTimestamp(System.currentTimeMillis());
        return new ConsumerRecord<>("topic", 0, 0L, key, value);
    }

    public static ConsumerRecord<String, TransactionResponse> getValidCompletedConsumerRecord(String transactionId) {
        String key = "key";
        TransactionResponse value = new TransactionResponse();
        value.setTransactionId(transactionId);
        value.setOperationType(OperationType.DEPOSIT);
        value.setTransactionStatus(TransactionStatus.COMPLETED);
        value.setTimestamp(System.currentTimeMillis());
        return new ConsumerRecord<>("topic", 0, 0L, key, value);
    }

    public static ConsumerRecord<String, TransactionResponse> getValidFailedConsumerRecord() {
        String key = "key";
        TransactionResponse value = new TransactionResponse();
        value.setTransactionId(UUID.randomUUID().toString());
        value.setOperationType(OperationType.DEPOSIT);
        value.setTransactionStatus(TransactionStatus.FAILED);
        value.setTimestamp(System.currentTimeMillis());
        return new ConsumerRecord<>("topic", 0, 0L, key, value);
    }

    public static ConsumerRecord<String, TransactionResponse> getValidFailedConsumerRecord(String transactionId) {
        String key = "key";
        TransactionResponse value = new TransactionResponse();
        value.setTransactionId(transactionId);
        value.setOperationType(OperationType.DEPOSIT);
        value.setTransactionStatus(TransactionStatus.FAILED);
        value.setTimestamp(System.currentTimeMillis());
        return new ConsumerRecord<>("topic", 0, 0L, key, value);
    }

    public static ConsumerRecord<String, TransactionResponse> getInvalidConsumerRecord() {
        String key = "key";
        TransactionResponse value = new TransactionResponse();
        value.setTransactionId("invalid-id");
        value.setOperationType(OperationType.DEPOSIT);
        value.setTransactionStatus(TransactionStatus.FAILED);
        value.setTimestamp(System.currentTimeMillis());
        return new ConsumerRecord<>("topic", 0, 0L, key, value);
    }

    public static TransactionRequest getValidTransactionRequest() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTransactionId(UUID.randomUUID().toString());
        transactionRequest.setOperationType(OperationType.DEPOSIT);
        transactionRequest.setAmount(BigDecimal.valueOf(85.58));
        transactionRequest.setCurrency("USD");
        transactionRequest.setTransactionStatus(TransactionStatus.PENDING);
        transactionRequest.setTimestamp(System.currentTimeMillis());
        transactionRequest.copyProperties(properties);
        return transactionRequest;
    }

    public static TransactionRequest getInvalidTransactionRequest() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTransactionId("invalid-id");
        transactionRequest.setOperationType(OperationType.DEPOSIT);
        transactionRequest.setAmount(BigDecimal.valueOf(-85.58));
        transactionRequest.setCurrency("TEST");
        transactionRequest.setTransactionStatus(TransactionStatus.PENDING);
        transactionRequest.setTimestamp(System.currentTimeMillis());
        transactionRequest.copyProperties(properties);
        return transactionRequest;
    }

    public static CancelTransactionDto getValidCancelTransactionDto() {
        CancelTransactionDto cancelTransactionDto = new CancelTransactionDto();
        cancelTransactionDto.setId(UUID.randomUUID().toString());
        cancelTransactionDto.setStatus(TransactionStatus.FAILED);
        return cancelTransactionDto;
    }

    public static CancelTransactionDto getInvalidCancelTransactionDto() {
        CancelTransactionDto cancelTransactionDto = new CancelTransactionDto();
        cancelTransactionDto.setId("invalid-id");
        cancelTransactionDto.setStatus(TransactionStatus.FAILED);
        return cancelTransactionDto;
    }

    public static TransactionRequestDto getValidTransactionRequestDto() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);
        properties.put(TIMESTAMP, System.currentTimeMillis() + "");

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto();
        transactionRequestDto.setOperationType(OperationType.DEPOSIT);
        transactionRequestDto.setAmount("15.58");
        transactionRequestDto.setCurrency("USD");
        transactionRequestDto.setProperties(properties);
        return transactionRequestDto;
    }

    public static TransactionRequestDto getInvalidTransactionRequestDto() {
        Map<String, String> properties = new HashMap<>();
        properties.put(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        properties.put(RECIPIENT_WALLET_ID, STR_WALLET_ID);
        properties.put(TIMESTAMP, System.currentTimeMillis() + "");

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto();
        transactionRequestDto.setOperationType(OperationType.DEPOSIT);
        transactionRequestDto.setAmount("-15.58");
        transactionRequestDto.setCurrency("TEST");
        transactionRequestDto.setProperties(properties);
        return transactionRequestDto;
    }
}
