package aq.project.test.handlers.wsdtrh.unit;

import aq.project.dto.PaymentProviderServiceCancelTransactionRequestDto;
import aq.project.dto.PaymentProviderServiceFailTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceDepositTransactionRequestDto;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.utils.handlers.wallet_service.request.WalletServiceDepositTransactionRequestHandler;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.mappers.WalletServiceTransactionDtoMapper;
import aq.project.utils.properties.PaymentProviderServiceKafkaProperties;
import aq.project.utils.properties.WalletServiceKafkaProperties;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;
import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidWalletServiceDepositTransactionRequest;

@ExtendWith(MockitoExtension.class)
public class HandleTransactionRequestUnitTest {

    @Spy
    private WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;
    @Spy
    private  PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private PaymentProviderServiceKafkaProperties paymentProviderServiceKafkaProperties;
    @Mock
    private WalletServiceKafkaProperties walletServiceKafkaProperties;

    @Mock
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    @Mock
    private WalletServiceDepositTransactionRequestRepository walletServiceDepositTransactionRequestRepository;

    @InjectMocks
    private WalletServiceDepositTransactionRequestHandler walletServiceDepositTransactionRequestHandler;

    @BeforeEach
    public void setUpFieldsAnnotatedAsValue() {
        ReflectionTestUtils.setField(walletServiceDepositTransactionRequestHandler, "serviceName", "test-service-name");
        ReflectionTestUtils.setField(walletServiceDepositTransactionRequestHandler, "merchantId", "test-merchant-id");
        ReflectionTestUtils.setField(walletServiceDepositTransactionRequestHandler, "paymentProviderServiceName", "payment-provider-service-name");
        ReflectionTestUtils.setField(walletServiceDepositTransactionRequestHandler, "walletServiceName", "wallet-service-name");
    }

    @Test
    public void successHandleTransactionRequest() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.setId(transactionId);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("wallet_service_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any()))
                .thenReturn(completableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));
        Assertions.assertTrue(walletServiceDepositTransactionRequest.getTransactionRequestMetadata().isProcessed());
        Assertions.assertEquals(TransactionStatus.PENDING, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(), Mockito.any(WalletServiceDepositTransactionRequestDto.class));
    }

    @Test
    public void failHandleTransactionRequestWhenTransactionServiceDepositTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("payment_service_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any()))
                .thenReturn(completableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));
        Assertions.assertTrue(walletServiceDepositTransactionRequest.getTransactionRequestMetadata().isProcessed());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleTransactionRequestWhenTransactionServiceDepositTransactionIsFailedStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);
        transactionServiceDepositTransaction.setId(transactionId);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("payment_service_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any()))
                .thenReturn(completableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));
        Assertions.assertTrue(walletServiceDepositTransactionRequest.getTransactionRequestMetadata().isProcessed());
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleTransactionRequestWhenTransactionServiceDepositTransactionIsCanceledStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.CANCELED);
        transactionServiceDepositTransaction.setId(transactionId);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("payment_service_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any()))
                .thenReturn(completableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));
        Assertions.assertTrue(walletServiceDepositTransactionRequest.getTransactionRequestMetadata().isProcessed());
        Assertions.assertEquals(TransactionStatus.CANCELED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class));
    }

    @Test
    public void failHandleTransactionRequestWhenTransactionServiceDepositTransactionIsCompletedStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.COMPLETED);
        transactionServiceDepositTransaction.setId(transactionId);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));
        Assertions.assertTrue(walletServiceDepositTransactionRequest.getTransactionRequestMetadata().isProcessed());
        Assertions.assertEquals(TransactionStatus.COMPLETED, transactionServiceDepositTransaction.getStatus());
    }

    @Test
    public void failHandleTransactionRequestWhenNotFoundWalletServiceDepositTransactionRequestTopic() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.setId(transactionId);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenThrow(NotFoundTopicException.class);
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("payment_service_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any()))
                .thenReturn(completableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));
        Assertions.assertTrue(walletServiceDepositTransactionRequest.getTransactionRequestMetadata().isProcessed());
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleTransactionRequestWhenExceptionOccurredDuringDepositTransactionRequestWalletServiceDtoSendToKafka() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);
        transactionServiceDepositTransaction.setId(transactionId);

        CompletableFuture<SendResult<String, Object>> corruptedCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> legalCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("payment_service_deposit_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.anyString()))
                .thenReturn("wallet_service_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any(WalletServiceDepositTransactionRequestDto.class)))
                .thenReturn(corruptedCompletableFuture);
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(legalCompletableFuture);
        Mockito.when(corruptedCompletableFuture.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceDepositTransactionRequestHandler.handleTransactionRequest(walletServiceDepositTransactionRequest));
        Assertions.assertTrue(walletServiceDepositTransactionRequest.getTransactionRequestMetadata().isProcessed());
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
    }
}
