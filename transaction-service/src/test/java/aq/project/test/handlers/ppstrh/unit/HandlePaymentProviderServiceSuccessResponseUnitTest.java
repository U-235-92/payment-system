package aq.project.test.handlers.ppstrh.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.payment_provider_service.PaymentProviderServiceTransactionRequestRepository;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.utils.handlers.payment_provider_service.request.PaymentProviderServiceTransactionRequestHandler;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.properties.PaymentProviderServiceKafkaProperties;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import io.opentelemetry.api.OpenTelemetry;
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static aq.project._utils.entities.payment_provider_service.PaymentProviderServiceEntities.getValidPaymentProviderServiceTransactionRequest;
import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;

@ExtendWith(MockitoExtension.class)
public class HandlePaymentProviderServiceSuccessResponseUnitTest {

    @Spy
    private PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private PaymentProviderServiceKafkaProperties paymentProviderServiceKafkaProperties;

    @Mock
    private PaymentProviderServiceTransactionRequestRepository paymentProviderServiceTransactionRequestRepository;

    @InjectMocks
    private PaymentProviderServiceTransactionRequestHandler paymentProviderServiceTransactionRequestHandler;

    @BeforeEach
    public void setUpFieldsAnnotatedAsValue() {
        ReflectionTestUtils.setField(paymentProviderServiceTransactionRequestHandler, "serviceName", "test-service-name");
    }

    @Test
    public void successHandlePaymentProviderServiceTransactionRequest() {
//        Arrange
        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = getValidPaymentProviderServiceTransactionRequest();
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("payment_provider_service_create_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any())).thenReturn(completableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceTransactionRequestHandler.handlePaymentProviderServiceCreateTransactionRequest(paymentProviderServiceTransactionRequest));
        Assertions.assertTrue(paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().isProcessed());
    }

    @Test
    public void failedScheduleHandleCreateTransactionOnPaymentProviderServiceWhenKafkaResponseError() throws ExecutionException, InterruptedException {
//        Arrange
        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = getValidPaymentProviderServiceTransactionRequest();
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("payment_provider_service_create_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any()))
                .thenReturn(completableFuture);
        Mockito.when(completableFuture.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceTransactionRequestHandler.handlePaymentProviderServiceCreateTransactionRequest(paymentProviderServiceTransactionRequest));
        Assertions.assertTrue(paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().isProcessed());
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());
    }

    @Test
    public void failedScheduleHandleCreateTransactionOnPaymentProviderServiceWhenEntityNotFoundExceptionOccurred() throws ExecutionException, InterruptedException {
//        Arrange
        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = getValidPaymentProviderServiceTransactionRequest();
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                        .thenReturn(Optional.empty());
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("payment_provider_service_create_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.any()))
                .thenReturn(completableFuture);
        Mockito.when(completableFuture.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceTransactionRequestHandler.handlePaymentProviderServiceCreateTransactionRequest(paymentProviderServiceTransactionRequest));
        Assertions.assertTrue(paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().isProcessed());
    }

    @Test
    public void failedScheduleHandleCreateTransactionOnPaymentProviderServiceWhenNotFoundTopicExceptionOccurred() throws ExecutionException, InterruptedException {
//        Arrange
        PaymentProviderServiceTransactionRequest paymentProviderServiceTransactionRequest = getValidPaymentProviderServiceTransactionRequest();
        paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().setProcessed(false);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenThrow(NotFoundTopicException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentProviderServiceTransactionRequestHandler.handlePaymentProviderServiceCreateTransactionRequest(paymentProviderServiceTransactionRequest));
        Assertions.assertTrue(paymentProviderServiceTransactionRequest.getTransactionRequestMetadata().isProcessed());
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());
    }
}
