package aq.project.message_broker_handler.unit;

import aq.project.dto.CancelTransactionDto;
import aq.project.dto.TransactionStatusDto;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ServiceHttpException;
import aq.project.messages.TransactionResponse;
import aq.project.payment_provider_service.WebhookApiClient;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.MessageBrokerHandler;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.telemetry.TraceContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.Entities.*;

@ExtendWith(MockitoExtension.class)
public class ProcessTransactionResponseUnitTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentServiceHandler paymentServiceHandler;

    @Mock
    private WebhookApiClient paymentServiceWebhookApiClient;

    @Mock
    private TraceContext traceContext;

    @InjectMocks
    private MessageBrokerHandler messageBrokerHandler;

    @Test
    public void successProcessCompletedTransactionResponseOnTransactionIsNotProcessedUnitTest() {
//        Arrange
        ReflectionTestUtils.setField(messageBrokerHandler, "merchantId", "id");
        ReflectionTestUtils.setField(messageBrokerHandler, "merchantSecret", "secret");

        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidCompletedConsumerRecord();
        Transaction transaction = getValidCompletedNotProcessedTransaction();

        String transactionStatus = transaction.getStatus().getValue();
        String consumerRecordTransactionStatus = consumerRecord.value().getTransactionStatus().getValue();

        Mockito.doReturn(Optional.of(transaction))
                .when(transactionRepository)
                .findById(Mockito.anyString());

        Mockito.doReturn(ResponseEntity.status(200).build())
                .when(paymentServiceWebhookApiClient)
                .updateTransactionStatus(Mockito.anyString(), Mockito.any(TransactionStatusDto.class), Mockito.anyString());

        Mockito.doReturn(UUID.randomUUID().toString())
                .when(traceContext)
                .getTraceId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> messageBrokerHandler.processTransactionResponse(consumerRecord));
        Assertions.assertTrue(transaction.isProcessed());
        Assertions.assertEquals(transactionStatus, consumerRecordTransactionStatus);

        Mockito.verify(paymentServiceWebhookApiClient)
                .updateTransactionStatus(Mockito.anyString(), Mockito.any(TransactionStatusDto.class), Mockito.anyString());
        Mockito.verify(transactionRepository)
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void successProcessCompletedTransactionResponseOnTransactionIsProcessedUnitTest() {
//        Arrange
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidCompletedConsumerRecord();
        Transaction transaction = getValidCompletedProcessedTransaction();

        Mockito.when(transactionRepository
                        .findById(Mockito.anyString()))
                .thenReturn(Optional.of(transaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> messageBrokerHandler.processTransactionResponse(consumerRecord));

        Mockito.verify(paymentServiceWebhookApiClient, Mockito.never())
                .updateTransactionStatus(Mockito.anyString(), Mockito.any(TransactionStatusDto.class), Mockito.anyString());
        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failProcessCompletedTransactionResponseOnTransactionIsNotProcessedAndPaymentServiceResponseErrorResponseUnitTest() {
//        Arrange
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidCompletedConsumerRecord();
        Transaction transaction = getValidCompletedNotProcessedTransaction();

        Mockito.doReturn(Optional.of(transaction))
                .when(transactionRepository)
                .findById(Mockito.anyString());

        Mockito.doThrow(ServiceHttpException.class)
                .when(paymentServiceWebhookApiClient)
                .updateTransactionStatus(Mockito.anyString(), Mockito.any(TransactionStatusDto.class), Mockito.anyString());

        Mockito.doReturn(UUID.randomUUID().toString())
                .when(traceContext)
                .getTraceId();

        ReflectionTestUtils.setField(messageBrokerHandler, "merchantId", "id");
        ReflectionTestUtils.setField(messageBrokerHandler, "merchantSecret", "secret");

//        Act & Assert
        Assertions.assertThrows(ServiceHttpException.class,
                () -> messageBrokerHandler.processTransactionResponse(consumerRecord));

        Mockito.verify(paymentServiceWebhookApiClient)
                .updateTransactionStatus(Mockito.anyString(), Mockito.any(TransactionStatusDto.class), Mockito.anyString());
        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void successProcessFailedTransactionResponseOnTransactionIsNotProcessedUnitTest() {
//        Arrange
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidFailedConsumerRecord();
        Transaction transaction = getValidFailedNotProcessedTransaction();

        String transactionStatus = transaction.getStatus().getValue();
        String consumerRecordTransactionStatus = consumerRecord.value().getTransactionStatus().getValue();

        Mockito.when(transactionRepository
                        .findById(Mockito.anyString()))
                .thenReturn(Optional.of(transaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> messageBrokerHandler.processTransactionResponse(consumerRecord));
        Assertions.assertTrue(transaction.isProcessed());
        Assertions.assertEquals(transactionStatus, consumerRecordTransactionStatus);

        Mockito.verify(paymentServiceHandler)
                .sendCancelTransactionRequestToPaymentService(Mockito.any(CancelTransactionDto.class));
        Mockito.verify(transactionRepository)
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void successProcessFailedTransactionResponseOnTransactionIsProcessedUnitTest() {
//        Arrange
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidFailedConsumerRecord();
        Transaction transaction = getValidFailedProcessedTransaction();

        Mockito.when(transactionRepository
                        .findById(Mockito.anyString()))
                .thenReturn(Optional.of(transaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> messageBrokerHandler.processTransactionResponse(consumerRecord));

        Mockito.verify(paymentServiceHandler, Mockito.never())
                .sendCancelTransactionRequestToPaymentService(Mockito.any(CancelTransactionDto.class));
        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failProcessFailedTransactionResponseOnTransactionIsNotProcessedAndPaymentServiceResponseErrorOnCancelTransactionUnitTest() {
//        Arrange
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidFailedConsumerRecord();
        Transaction transaction = getValidFailedNotProcessedTransaction();

        Mockito.when(transactionRepository
                        .findById(Mockito.anyString()))
                .thenReturn(Optional.of(transaction));
        Mockito.doThrow(ServiceHttpException.class).when(paymentServiceHandler)
                .sendCancelTransactionRequestToPaymentService(Mockito.any(CancelTransactionDto.class));

//        Act & Assert
        Assertions.assertThrows(ServiceHttpException.class,
                () -> messageBrokerHandler.processTransactionResponse(consumerRecord));

        Mockito.verify(paymentServiceHandler)
                .sendCancelTransactionRequestToPaymentService(Mockito.any(CancelTransactionDto.class));
        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failProcessCompletedTransactionResponseOnTransactionDoesNotExistUnitTest() {
//        Arrange
        ConsumerRecord<String, TransactionResponse> consumerRecord = getValidCompletedConsumerRecord();

        Mockito.when(transactionRepository
                        .findById(Mockito.anyString()))
                .thenThrow(EntityNotFoundException.class);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> messageBrokerHandler.processTransactionResponse(consumerRecord));

        Mockito.verify(paymentServiceWebhookApiClient, Mockito.never())
                .updateTransactionStatus(Mockito.anyString(), Mockito.any(TransactionStatusDto.class), Mockito.anyString());
        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
    }
}
