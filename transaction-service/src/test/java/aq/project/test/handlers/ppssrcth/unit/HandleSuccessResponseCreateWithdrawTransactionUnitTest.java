package aq.project.test.handlers.ppssrcth.unit;

import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceSuccessHandleTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceWithdrawTransactionRequestRepository;
import aq.project.utils.handlers.payment_provider_service.response.PaymentProviderServiceSuccessResponseCreateTransactionHandler;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.mappers.WalletServiceTransactionDtoMapper;
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

import static aq.project._utils.entities.payment_provider_service.PaymentProviderServiceEntities.getValidPaymentProviderServiceSuccessHandleTransactionDto;
import static aq.project._utils.entities.transaction_service.WithdrawTransactionServiceEntities.getValidTransactionServiceWithdrawTransaction;

@ExtendWith(MockitoExtension.class)
public class HandleSuccessResponseCreateWithdrawTransactionUnitTest {

    @Spy
    private PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;
    @Spy
    private WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Mock
    private TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    @Mock
    private WalletServiceWithdrawTransactionRequestRepository walletServiceWithdrawTransactionRequestRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private PaymentProviderServiceKafkaProperties paymentProviderServiceKafkaProperties;

    @InjectMocks
    private PaymentProviderServiceSuccessResponseCreateTransactionHandler paymentProviderServiceSuccessResponseCreateTransactionHandler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(paymentProviderServiceSuccessResponseCreateTransactionHandler, "serviceName", "service-name");
        ReflectionTestUtils.setField(paymentProviderServiceSuccessResponseCreateTransactionHandler, "paymentProviderServiceName", "payment-provider-service-name");
        ReflectionTestUtils.setField(paymentProviderServiceSuccessResponseCreateTransactionHandler, "merchantId", "merchant-id");
    }

    @Test
    public void successHandleSuccessResponseCreateTransactionOnPaymentProviderService() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceSuccessHandleTransactionDto successHandleTransactionDto = getValidPaymentProviderServiceSuccessHandleTransactionDto();
        successHandleTransactionDto.setTransactionId(transactionId);
        successHandleTransactionDto.setOperation(Operation.WITHDRAW);

        TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction = getValidTransactionServiceWithdrawTransaction();
        transactionServiceWithdrawTransaction.setId(transactionId);
        transactionServiceWithdrawTransaction.setStatus(TransactionStatus.PENDING);

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(transactionServiceWithdrawTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceSuccessResponseCreateTransactionHandler
                        .handleSuccessResponseCreateTransactionOnPaymentProviderService(
                                successHandleTransactionDto));

        Mockito.verify(walletServiceWithdrawTransactionRequestRepository, Mockito.times(1))
                .save(Mockito.any());
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnPaymentProviderServiceWhenTransactionServiceDepositTransactionNotFound() {
//        Arrange
        PaymentProviderServiceSuccessHandleTransactionDto successHandleTransactionDto = getValidPaymentProviderServiceSuccessHandleTransactionDto();
        successHandleTransactionDto.setOperation(Operation.WITHDRAW);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any()))
                .thenReturn(completableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceSuccessResponseCreateTransactionHandler
                        .handleSuccessResponseCreateTransactionOnPaymentProviderService(
                                successHandleTransactionDto));

        Mockito.verify(walletServiceWithdrawTransactionRequestRepository, Mockito.never())
                .save(Mockito.any());
//        TODO: replace on times(2) after realise service [webhook-service]
        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(), Mockito.any());
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnPaymentProviderServiceWhenTransactionServiceDepositTransactionStatusIsFailed() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceSuccessHandleTransactionDto successHandleTransactionDto = getValidPaymentProviderServiceSuccessHandleTransactionDto();
        successHandleTransactionDto.setTransactionId(transactionId);
        successHandleTransactionDto.setOperation(Operation.WITHDRAW);

        TransactionServiceWithdrawTransaction validTransactionServiceWithdrawTransaction = getValidTransactionServiceWithdrawTransaction();
        validTransactionServiceWithdrawTransaction.setId(transactionId);
        validTransactionServiceWithdrawTransaction.setStatus(TransactionStatus.FAILED);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(validTransactionServiceWithdrawTransaction));
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any()))
                .thenReturn(completableFuture);
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_fail_transaction_request");

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceSuccessResponseCreateTransactionHandler
                        .handleSuccessResponseCreateTransactionOnPaymentProviderService(
                                successHandleTransactionDto));

        Mockito.verify(walletServiceWithdrawTransactionRequestRepository, Mockito.never())
                .save(Mockito.any());
//        TODO: replace on times(2) after realise service [webhook-service]
        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(), Mockito.any());
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnPaymentProviderServiceWhenTransactionServiceDepositTransactionStatusIsCancelled() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceSuccessHandleTransactionDto successHandleTransactionDto = getValidPaymentProviderServiceSuccessHandleTransactionDto();
        successHandleTransactionDto.setTransactionId(transactionId);
        successHandleTransactionDto.setOperation(Operation.WITHDRAW);

        TransactionServiceWithdrawTransaction validTransactionServiceWithdrawTransaction = getValidTransactionServiceWithdrawTransaction();
        validTransactionServiceWithdrawTransaction.setId(transactionId);
        validTransactionServiceWithdrawTransaction.setStatus(TransactionStatus.CANCELED);

        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(validTransactionServiceWithdrawTransaction));
        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any()))
                .thenReturn(completableFuture);
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenReturn("payment_provider_service_cancel_transaction_request");

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceSuccessResponseCreateTransactionHandler
                        .handleSuccessResponseCreateTransactionOnPaymentProviderService(
                                successHandleTransactionDto));

        Mockito.verify(walletServiceWithdrawTransactionRequestRepository, Mockito.never())
                .save(Mockito.any());
//        TODO: replace on times(2) after realise service [webhook-service]
        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.any(), Mockito.any());
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnPaymentProviderServiceWhenTransactionServiceDepositTransactionStatusIsCompleted() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceSuccessHandleTransactionDto successHandleTransactionDto = getValidPaymentProviderServiceSuccessHandleTransactionDto();
        successHandleTransactionDto.setTransactionId(transactionId);
        successHandleTransactionDto.setOperation(Operation.WITHDRAW);

        TransactionServiceWithdrawTransaction validTransactionServiceWithdrawTransaction = getValidTransactionServiceWithdrawTransaction();
        validTransactionServiceWithdrawTransaction.setId(transactionId);
        validTransactionServiceWithdrawTransaction.setStatus(TransactionStatus.COMPLETED);

//        TODO: uncomment after realise service [webhook-service]
//        CompletableFuture<SendResult<String, Object>> completableFuture = Mockito.mock(CompletableFuture.class);

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(validTransactionServiceWithdrawTransaction));
//        TODO: uncomment after realise service [webhook-service]
//        Mockito.when(kafkaTemplate.send(Mockito.any(), Mockito.any()))
//                .thenReturn(completableFuture);

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceSuccessResponseCreateTransactionHandler
                        .handleSuccessResponseCreateTransactionOnPaymentProviderService(
                                successHandleTransactionDto));

        Mockito.verify(walletServiceWithdrawTransactionRequestRepository, Mockito.never())
                .save(Mockito.any());
//        TODO: replace on times(2) after realise service [webhook-service]
//        Mockito.verify(kafkaTemplate, Mockito.times(1))
//                .send(Mockito.any(), Mockito.any());
    }

    @Test
    public void failHandleSuccessResponseCreateTransactionOnPaymentProviderServiceWhenNotFoundTopicException() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        PaymentProviderServiceSuccessHandleTransactionDto successHandleTransactionDto = getValidPaymentProviderServiceSuccessHandleTransactionDto();
        successHandleTransactionDto.setTransactionId(transactionId);
        successHandleTransactionDto.setOperation(Operation.WITHDRAW);

        TransactionServiceWithdrawTransaction validTransactionServiceWithdrawTransaction = getValidTransactionServiceWithdrawTransaction();
        validTransactionServiceWithdrawTransaction.setId(transactionId);
        validTransactionServiceWithdrawTransaction.setStatus(TransactionStatus.FAILED);

        Mockito.when(transactionServiceWithdrawTransactionRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(validTransactionServiceWithdrawTransaction));
        Mockito.when(paymentProviderServiceKafkaProperties.getTopic(Mockito.any()))
                .thenThrow(NotFoundTopicException.class);

//        Act & Assert
        Assertions.assertDoesNotThrow(
                () -> paymentProviderServiceSuccessResponseCreateTransactionHandler
                        .handleSuccessResponseCreateTransactionOnPaymentProviderService(
                                successHandleTransactionDto));

        Mockito.verify(walletServiceWithdrawTransactionRequestRepository, Mockito.never())
                .save(Mockito.any());
        Mockito.verify(kafkaTemplate, Mockito.never())
                .send(Mockito.any(), Mockito.any());
    }
}
