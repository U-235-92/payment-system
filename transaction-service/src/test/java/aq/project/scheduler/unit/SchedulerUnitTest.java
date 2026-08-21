package aq.project.scheduler.unit;

import aq.project.dto.CancelTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.exceptions.ExceedAttemptLimitException;
import aq.project.exceptions.ServiceHttpException;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.MessageBrokerHandler;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.shedulers.Scheduler;
import aq.project.utils.telemetry.TraceContext;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static aq.project._utils.Entities.getValidPendingNotProcessedTransaction;

@ExtendWith(MockitoExtension.class)
public class SchedulerUnitTest {

    @Spy
    private final OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MessageBrokerHandler messageBrokerHandler;

    @Mock
    private PaymentServiceHandler paymentServiceHandler;

    @Mock
    private TraceContext traceContext;

    @InjectMocks
    private Scheduler scheduler;

    @Test
    public void successScheduleSendTransactionRequestToMessageBrokerUnitTest() {
//        Arrange
        Transaction transaction = getValidPendingNotProcessedTransaction();

        Mockito.when(transactionRepository.findByStatus(Mockito.any(TransactionStatus.class)))
                .thenReturn(List.of(transaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> scheduler.scheduleSendTransactionRequestToMessageBroker());

        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any(Transaction.class));
    }

    @Test
    public void failScheduleSendTransactionRequestToMessageBrokerOnExceedAttemptLimitExceptionUnitTest() throws ExecutionException, InterruptedException {
//        Arrange
        Transaction transaction = getValidPendingNotProcessedTransaction();

        Mockito.when(transactionRepository.findByStatus(Mockito.any(TransactionStatus.class)))
                .thenReturn(List.of(transaction));
        Mockito.doThrow(ExceedAttemptLimitException.class)
                .when(messageBrokerHandler)
                .sendTransactionRequestToMessageBroker(Mockito.any(Transaction.class));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> scheduler.scheduleSendTransactionRequestToMessageBroker());

        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failScheduleSendTransactionRequestToMessageBrokerOnExecutionExceptionUnitTest() throws ExecutionException, InterruptedException {
//        Arrange
        Transaction transaction = getValidPendingNotProcessedTransaction();

        Mockito.when(transactionRepository.findByStatus(Mockito.any(TransactionStatus.class)))
                .thenReturn(List.of(transaction));
        Mockito.doThrow(ExecutionException.class)
                .when(messageBrokerHandler)
                .sendTransactionRequestToMessageBroker(Mockito.any(Transaction.class));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> scheduler.scheduleSendTransactionRequestToMessageBroker());

        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failScheduleSendTransactionRequestToMessageBrokerOnIllegalArgumentExceptionUnitTest() throws ExecutionException, InterruptedException {
//        Arrange
        Transaction transaction = getValidPendingNotProcessedTransaction();

        Mockito.when(transactionRepository.findByStatus(Mockito.any(TransactionStatus.class)))
                .thenReturn(List.of(transaction));
        Mockito.doThrow(IllegalArgumentException.class)
                .when(messageBrokerHandler)
                .sendTransactionRequestToMessageBroker(Mockito.any(Transaction.class));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> scheduler.scheduleSendTransactionRequestToMessageBroker());

        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failScheduleSendTransactionRequestToMessageBrokerOnExceedAttemptLimitExceptionAndWhenCancelTransactionRequestFailedUnitTest() throws ExceedAttemptLimitException, ExecutionException, InterruptedException {
//        Arrange
        Transaction transaction = getValidPendingNotProcessedTransaction();

        Mockito.when(transactionRepository.findByStatus(Mockito.any(TransactionStatus.class)))
                .thenReturn(List.of(transaction));
        Mockito.doThrow(ExceedAttemptLimitException.class)
                .when(messageBrokerHandler)
                .sendTransactionRequestToMessageBroker(Mockito.any(Transaction.class));
        Mockito.doThrow(ServiceHttpException.class)
                .when(paymentServiceHandler)
                .sendCancelTransactionRequestToPaymentService(Mockito.any(CancelTransactionDto.class));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> scheduler.scheduleSendTransactionRequestToMessageBroker());

        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
    }
}
