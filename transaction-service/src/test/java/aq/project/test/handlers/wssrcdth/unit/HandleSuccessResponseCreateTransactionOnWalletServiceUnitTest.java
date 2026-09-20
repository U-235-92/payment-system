package aq.project.test.handlers.wssrcdth.unit;

import aq.project.dto.*;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.utils.handlers.wallet_service.response.deposit_transaction.WalletServiceSuccessResponseCreateDepositTransactionHandler;
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
import static aq.project._utils.entities.wallet_service.WalletServiceSuccessResponseCreateDepositTransactionHandlerEntities.getValidWalletServiceDepositTransactionSuccessResponseDto;

@ExtendWith(MockitoExtension.class)
public class HandleSuccessResponseCreateTransactionOnWalletServiceUnitTest {

    @Spy
    private WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;
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
    private WalletServiceKafkaProperties walletServiceKafkaProperties;

    @Mock
    private TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;

    @InjectMocks
    private WalletServiceSuccessResponseCreateDepositTransactionHandler walletServiceSuccessResponseCreateDepositTransactionHandler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(walletServiceSuccessResponseCreateDepositTransactionHandler, "serviceName", "testService");
        ReflectionTestUtils.setField(walletServiceSuccessResponseCreateDepositTransactionHandler, "paymentProviderServiceName", "testPaymentProviderService");
        ReflectionTestUtils.setField(walletServiceSuccessResponseCreateDepositTransactionHandler, "walletServiceName", "testWalletServiceName");
        ReflectionTestUtils.setField(walletServiceSuccessResponseCreateDepositTransactionHandler, "merchantId", "testMerchantId");
    }

    @Test
    public void successHandleSuccessResponseCreateTransactionOnWalletService() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.COMPLETED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(transactionServiceDepositTransactionRepository, Mockito.times(1))
                .save(Mockito.any(TransactionServiceDepositTransaction.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInFailState() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_fail_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInFailStateAndExceptionOccurredWhileSendFailRequestToPaymentProviderService() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_fail_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(paymentProviderServiceCompletableFuture.get())
                .thenThrow(ExecutionException.class);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInFailStateAndNotFoundTopicExceptionOccurredWhileSendFailRequestToPaymentProviderService() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenThrow(NotFoundTopicException.class);
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_fail_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.never())
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInFailStateAndExceptionOccurredWhileSendFailRequestToWalletService() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_fail_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);
        Mockito.when(walletServiceCompletableFuture.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInFailStateAndNotFoundTopicExceptionOccurredWhileSendFailRequestToWalletService() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenThrow(NotFoundTopicException.class);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate, Mockito.never())
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInCanceledState() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.CANCELED);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_cancel_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_cancel_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceCancelTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceCancelTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInCanceledStateAndExceptionOccurredWhileSendCancelRequestToPaymentProviderService() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.CANCELED);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_cancel_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_cancel_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(paymentProviderServiceCompletableFuture.get())
                .thenThrow(ExecutionException.class);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceCancelTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceCancelTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInCanceledStateAndNotFoundTopicExceptionOccurredWhileSendCancelRequestToPaymentProviderService() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.CANCELED);

        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenThrow(NotFoundTopicException.class);
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_cancel_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceCancelTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.never())
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceCancelTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInCanceledStateAndExceptionOccurredWhileSendCancelRequestToWalletService() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.CANCELED);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_cancel_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_cancel_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceCancelTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);
        Mockito.when(walletServiceCompletableFuture.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceCancelTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionInCancelStateAndNotFoundTopicExceptionOccurredWhileSendCancelRequestToWalletService() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        TransactionServiceDepositTransaction transactionServiceDepositTransaction = getValidTransactionServiceDepositTransaction();
        transactionServiceDepositTransaction.setId(transactionId);
        transactionServiceDepositTransaction.setStatus(TransactionStatus.CANCELED);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transactionServiceDepositTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_cancel_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenThrow(NotFoundTopicException.class);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, transactionServiceDepositTransaction.getStatus());

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceCancelTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate, Mockito.never())
                .send(Mockito.any(String.class), Mockito.any(WalletServiceCancelTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionNotFound() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_fail_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionNotFoundAndExceptionOccurredWhileSendPaymentProviderFailTransactionRequestToKafka() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_fail_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);
        Mockito.when(paymentProviderServiceCompletableFuture.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionNotFoundAndNotFoundTopicExceptionOccurredWhileSendPaymentProviderFailTransactionRequestToKafka() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenThrow(NotFoundTopicException.class);
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_fail_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));

        Mockito.verify(kafkaTemplate,  Mockito.never())
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionNotFoundAndExceptionOccurredWhileSendWalletServiceFailTransactionRequestToKafka() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);
        CompletableFuture<SendResult<String, Object>> walletServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("wallet_service_fail_deposit_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class)))
                .thenReturn(walletServiceCompletableFuture);
        Mockito.when(walletServiceCompletableFuture.get())
                .thenThrow(ExecutionException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));

        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnWalletServiceWhenTransactionServiceDepositTransactionNotFoundAndNotFoundTopicExceptionOccurredWhileSendWalletServiceFailTransactionRequestToKafka() throws ExecutionException, InterruptedException {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        WalletServiceDepositTransactionSuccessResponseDto depositTransactionResponseWalletServiceDto = getValidWalletServiceDepositTransactionSuccessResponseDto();
        depositTransactionResponseWalletServiceDto.setTransactionId(transactionId);

        CompletableFuture<SendResult<String, Object>> paymentProviderServiceCompletableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceDepositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());
        Mockito.when(walletServiceKafkaProperties.getTopic(Mockito.any()))
                .thenThrow(NotFoundTopicException.class);
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");
        Mockito.when(kafkaTemplate.send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class)))
                .thenReturn(paymentProviderServiceCompletableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceSuccessResponseCreateDepositTransactionHandler
                .handleSuccessResponseCreateTransactionOnWalletService(depositTransactionResponseWalletServiceDto));

        Mockito.verify(kafkaTemplate,  Mockito.never())
                .send(Mockito.any(String.class), Mockito.any(WalletServiceFailTransactionRequestDto.class));
        Mockito.verify(kafkaTemplate,  Mockito.times(1))
                .send(Mockito.any(String.class), Mockito.any(PaymentProviderServiceFailTransactionRequestDto.class));
    }
}
