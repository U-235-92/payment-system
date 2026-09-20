package aq.project.test.handlers.wsfrcdth.unit;

import aq.project.dto.PaymentProviderServiceCancelTransactionRequestDto;
import aq.project.dto.PaymentProviderServiceFailTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceTransactionErrorResponseDto;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.utils.handlers.wallet_service.response.deposit_transaction.WalletServiceFailResponseCreateDepositTransactionHandler;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static aq.project._utils.entities.transaction_service.DepositTransactionServiceEntities.getValidTransactionServiceDepositTransaction;
import static aq.project._utils.entities.wallet_service.WalletServiceFailResponseCreateDepositTransactionHandlerEntities.getValidWalletServiceTransactionErrorResponseDto;

@ExtendWith(MockitoExtension.class)
public class HandleErrorResponseCreateTransactionOnWalletServiceUnitTest {

    @Spy
    private PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private PaymentProviderServiceKafkaProperties paymentProviderServiceKafkaProperties;

    @Mock
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;

    @InjectMocks
    private WalletServiceFailResponseCreateDepositTransactionHandler walletServiceFailResponseCreateDepositTransactionHandler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(walletServiceFailResponseCreateDepositTransactionHandler, "merchantId", "testMerchantId");
        ReflectionTestUtils.setField(walletServiceFailResponseCreateDepositTransactionHandler, "serviceName", "testService");
        ReflectionTestUtils.setField(walletServiceFailResponseCreateDepositTransactionHandler, "paymentProviderServiceName", "testPaymentProviderService");
    }

    @Test
    public void successHandleErrorResponseCreateTransactionOnWalletService() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);

        WalletServiceTransactionErrorResponseDto walletServiceTransactionErrorResponseDto = getValidWalletServiceTransactionErrorResponseDto();
        walletServiceTransactionErrorResponseDto.setTransactionId(transactionId);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceFailResponseCreateDepositTransactionHandler
                .handleErrorResponseCreateTransactionOnWalletService(walletServiceTransactionErrorResponseDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(transactionServiceDepositTransactionRepository, Mockito.times(1))
                .save(Mockito.any(TransactionServiceDepositTransaction.class));
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInFailState() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        WalletServiceTransactionErrorResponseDto walletServiceTransactionErrorResponseDto = getValidWalletServiceTransactionErrorResponseDto();
        walletServiceTransactionErrorResponseDto.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceFailResponseCreateDepositTransactionHandler
                .handleErrorResponseCreateTransactionOnWalletService(walletServiceTransactionErrorResponseDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInCanceledState() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.CANCELED);

        WalletServiceTransactionErrorResponseDto walletServiceTransactionErrorResponseDto = getValidWalletServiceTransactionErrorResponseDto();
        walletServiceTransactionErrorResponseDto.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_cancel_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceFailResponseCreateDepositTransactionHandler
                .handleErrorResponseCreateTransactionOnWalletService(walletServiceTransactionErrorResponseDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class));
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInFailStateAndExceptionOccurredWhenSendFailTransactionRequestPaymentProviderServiceDtoToKafka() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        WalletServiceTransactionErrorResponseDto walletServiceTransactionErrorResponseDto = getValidWalletServiceTransactionErrorResponseDto();
        walletServiceTransactionErrorResponseDto.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(paymentProviderServiceCompletableFuture.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceFailResponseCreateDepositTransactionHandler
                .handleErrorResponseCreateTransactionOnWalletService(walletServiceTransactionErrorResponseDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleFailResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInFailStateAndNotFoundTopicExceptionOccurred() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        WalletServiceTransactionErrorResponseDto walletServiceTransactionErrorResponseDto = getValidWalletServiceTransactionErrorResponseDto();
        walletServiceTransactionErrorResponseDto.setTransactionId(transactionId);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenThrow(NotFoundTopicException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceFailResponseCreateDepositTransactionHandler
                .handleErrorResponseCreateTransactionOnWalletService(walletServiceTransactionErrorResponseDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.never())
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleErrorResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceTransactionErrorResponseDto walletServiceTransactionErrorResponseDto = getValidWalletServiceTransactionErrorResponseDto();
        walletServiceTransactionErrorResponseDto.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceFailResponseCreateDepositTransactionHandler
                .handleErrorResponseCreateTransactionOnWalletService(walletServiceTransactionErrorResponseDto));

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
    }
}
