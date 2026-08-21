package aq.project.message_broker_handler.unit;

import aq.project.entities.Transaction;
import aq.project.exceptions.ExceedAttemptLimitException;
import aq.project.messages.TransactionRequest;
import aq.project.utils.handlers.MessageBrokerHandler;
import aq.project.utils.telemetry.TraceContext;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static aq.project._utils.Entities.getValidPendingNotProcessedTransaction;

@ExtendWith(MockitoExtension.class)
public class SendTransactionRequestToMessageBrokerUnitTest {

    @Mock
    private KafkaTemplate<String, TransactionRequest> kafkaTemplate;

    @Mock
    private TraceContext traceContext;

    @InjectMocks
    private MessageBrokerHandler messageBrokerHandler;

    @BeforeEach
    public void setUp() {
        String walletOperationRequestTopicFieldName = "walletOperationRequestTopicName";
        String walletOperationRequestTopicFieldValue = "walletOperationRequestTopic";

        String depositPartitionFieldName = "depositPartitionName";
        String depositPartitionFieldValue = "depositPartition";

        String withdrawPartitionFieldName = "withdrawPartitionName";
        String withdrawPartitionFieldValue = "withdrawPartition";

        String transferPartitionFieldName = "transferPartitionName";
        String transferPartitionFieldValue = "transferPartition";

        ReflectionTestUtils.setField(messageBrokerHandler, walletOperationRequestTopicFieldName, walletOperationRequestTopicFieldValue);
        ReflectionTestUtils.setField(messageBrokerHandler, depositPartitionFieldName, depositPartitionFieldValue);
        ReflectionTestUtils.setField(messageBrokerHandler, withdrawPartitionFieldName, withdrawPartitionFieldValue);
        ReflectionTestUtils.setField(messageBrokerHandler, transferPartitionFieldName, transferPartitionFieldValue);
    }

    @Test
    public void successSendTransactionRequestToMessageBrokerUnitTest() {
//        Arrange
        String maxNumberAttemptsFieldName = "maxNumberAttemptsToSendTransactionRequestToMessageBroker";
        int maxNumberAttemptsFieldValue = 3;

        ReflectionTestUtils.setField(messageBrokerHandler, maxNumberAttemptsFieldName, maxNumberAttemptsFieldValue);

        CompletableFuture<SendResult<String, TransactionRequest>> future = Mockito.mock(CompletableFuture.class);

        Mockito.when(kafkaTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(future);
        Mockito.when(traceContext.getTraceId()).thenReturn(UUID.randomUUID().toString());

        Transaction transaction = getValidPendingNotProcessedTransaction();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> messageBrokerHandler.sendTransactionRequestToMessageBroker(transaction));

        Mockito.verify(kafkaTemplate, Mockito.only()).send(Mockito.any(ProducerRecord.class));
    }

    @Test
    public void failSendTransactionRequestToMessageBrokerOnExceedMaxNumberAttemptsUnitTest() {
//        Arrange
        String maxNumberAttemptsFieldName = "maxNumberAttemptsToSendTransactionRequestToMessageBroker";
        int maxNumberAttemptsFieldValue = 0;

        ReflectionTestUtils.setField(messageBrokerHandler, maxNumberAttemptsFieldName, maxNumberAttemptsFieldValue);

        Transaction transaction = getValidPendingNotProcessedTransaction();

//        Act & Assert
        Assertions.assertThrows(ExceedAttemptLimitException.class,
                () -> messageBrokerHandler.sendTransactionRequestToMessageBroker(transaction));

        Mockito.verify(kafkaTemplate, Mockito.never()).send(Mockito.any(ProducerRecord.class));
    }

    @Test
    public void failSendTransactionRequestToMessageBrokerOnErrorKafkaResponseUnitTest() throws ExecutionException, InterruptedException {
//        Arrange
        String maxNumberAttemptsFieldName = "maxNumberAttemptsToSendTransactionRequestToMessageBroker";
        int maxNumberAttemptsFieldValue = 3;

        ReflectionTestUtils.setField(messageBrokerHandler, maxNumberAttemptsFieldName, maxNumberAttemptsFieldValue);

        CompletableFuture<SendResult<String, TransactionRequest>> future = Mockito.mock(CompletableFuture.class);

        Mockito.when(future.get()).thenThrow(ExecutionException.class);
        Mockito.when(kafkaTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(future);
        Mockito.when(traceContext.getTraceId()).thenReturn(UUID.randomUUID().toString());

        Transaction transaction = getValidPendingNotProcessedTransaction();

//        Act & Assert
        Assertions.assertThrows(ExecutionException.class,
                () -> messageBrokerHandler.sendTransactionRequestToMessageBroker(transaction));
    }
}
