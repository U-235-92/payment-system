package aq.project.handlers.create_transaction_handler.unit;

import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceCreateTransactionRequestDto;
import aq.project.dto.PaymentProviderServiceErrorHandleTransactionDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.DtoConstraintsException;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.CreateTransactionHandler;
import aq.project.utils.mappers.TransactionMapper;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.validation.ConstraintViolationException;
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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static aq.project._utils.entities.CreateTransactionHandlerEntities.*;

@ExtendWith(MockitoExtension.class)
public class HandleCreateTransactionRequestUnitTest {

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
    public void successHandleCreateTransactionRequest() {
//        Arrange
        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getValidPaymentProviderServiceCreateTransactionRequestDto();;
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);

        Merchant merchant = getValidMerchant();

        Mockito.when(transactionRepository.existsById(Mockito.any()))
                .thenReturn(false);
        Mockito.when(merchantRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(merchant));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> createTransactionHandler
                .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));

        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failHandleCreateTransactionRequestOnConstraintViolationException() {
//        Arrange
        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getInvalidPaymentProviderServiceCreateTransactionRequestDto();;
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> createTransactionHandler
                        .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));

        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failHandleCreateTransactionRequestOnDtoConstraintsException() {
//        Arrange
        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getValidPaymentProviderServiceCreateTransactionRequestDto();
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);
        paymentProviderServiceCreateTransactionRequestDto.setAmount(BigDecimal.valueOf(-100.00));

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any()))
                .thenReturn(future);

//        Act & Assert
        Assertions.assertThrows(DtoConstraintsException.class,
                () -> createTransactionHandler
                        .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));

        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.anyString(), Mockito.any(PaymentProviderServiceErrorHandleTransactionDto.class));
    }

    @Test
    public void failHandleCreateTransactionRequestOnDtoConstraintsExceptionAndExecutionExceptionWhileSendResponseToKafka() throws ExecutionException, InterruptedException {
//        Arrange
        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getValidPaymentProviderServiceCreateTransactionRequestDto();
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);
        paymentProviderServiceCreateTransactionRequestDto.setAmount(BigDecimal.valueOf(-100.00));

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any()))
                .thenReturn(future);
        Mockito.when(future.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertThrows(DtoConstraintsException.class,
                () -> createTransactionHandler
                        .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));

        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.anyString(), Mockito.any(PaymentProviderServiceErrorHandleTransactionDto.class));
    }

    @Test
    public void failHandleCreateTransactionRequestOnTransactionAlreadyExists() {
//        Arrange
        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getValidPaymentProviderServiceCreateTransactionRequestDto();
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionRepository.existsById(Mockito.any()))
                .thenReturn(true);
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any()))
                .thenReturn(future);

//        Act & Assert
        Assertions.assertThrows(EntityAlreadyExistsException.class,
                () -> createTransactionHandler
                        .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));

        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.anyString(), Mockito.any(PaymentProviderServiceErrorHandleTransactionDto.class));

    }

    @Test
    public void failHandleCreateTransactionRequestOnMerchantNotFound() {
//        Arrange
        PaymentProviderServiceCreateTransactionRequestDto paymentProviderServiceCreateTransactionRequestDto = getValidPaymentProviderServiceCreateTransactionRequestDto();
        paymentProviderServiceCreateTransactionRequestDto.setOperation(Operation.DEPOSIT);

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionRepository.existsById(Mockito.any()))
                .thenReturn(false);
        Mockito.when(merchantRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any()))
                .thenReturn(future);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> createTransactionHandler
                        .handleCreateTransactionRequest(paymentProviderServiceCreateTransactionRequestDto));

        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.anyString(), Mockito.any(PaymentProviderServiceErrorHandleTransactionDto.class));

    }
}
