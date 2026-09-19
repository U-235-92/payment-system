package aq.project.handlers.create_transaction_handler.unit;

import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceSuccessHandleTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.CreateTransactionHandler;
import aq.project.utils.mappers.TransactionMapper;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static aq.project._utils.entities.CreateTransactionHandlerEntities.getValidTransaction;

@ExtendWith(MockitoExtension.class)
public class HandleScheduleCreateTransactionUnitTest {

    @Spy
    private TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CreateTransactionHandler createTransactionHandler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(createTransactionHandler, "serviceName", "serviceName");
        ReflectionTestUtils.setField(createTransactionHandler, "createTransactionResponseTopic", "createTransactionResponseTopic");
        ReflectionTestUtils.setField(createTransactionHandler, "createTransactionResponseExceptionsTopic", "createTransactionResponseExceptionsTopic");
    }

    @Test
    public void successHandleScheduleCreateTransaction() {
//        Arrange
        Transaction transaction = getValidTransaction();
        transaction.setOperation(Operation.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any()))
                .thenReturn(future);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> createTransactionHandler.handleScheduleCreateTransaction(transaction));
        Assertions.assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.anyString(), Mockito.any(PaymentProviderServiceSuccessHandleTransactionDto.class));
        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failHandleScheduleCreateTransactionOnExceptionWhenSendResponseToKafka() throws ExecutionException, InterruptedException {
//        Arrange
        Transaction transaction = getValidTransaction();
        transaction.setOperation(Operation.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any()))
                .thenReturn(future);
        Mockito.when(future.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> createTransactionHandler.handleScheduleCreateTransaction(transaction));
        Assertions.assertEquals(TransactionStatus.REQUIRED_MANUAL_HANDLE, transaction.getStatus());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.anyString(), Mockito.any(PaymentProviderServiceSuccessHandleTransactionDto.class));
        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }
}
