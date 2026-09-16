package aq.project.test.services.deposit_transaction_service.unit;

import aq.project.dto.WalletServiceDepositTransactionRequestDto;
import aq.project.dto.WalletServiceTransactionResponseErrorDto;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.services.transaction.DepositTransactionService;
import aq.project.services.wallet.WalletService;
import aq.project.utils.handlers.TransactionHandler;
import aq.project.utils.mappers.transaction.TransactionRequestMapper;
import aq.project.utils.mappers.transaction.TransactionResponseMapper;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import aq.project.utils.telemetry.TraceContext;
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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static aq.project._utils.TransactionRequests.getValidDepositTransactionRequest;
import static aq.project._utils.WalletEntities.getValidWallet;
import static aq.project._utils.WalletEntities.getValidWalletBlocked;

@ExtendWith(MockitoExtension.class)
public class HandleTransactionRequestUnitTest {

    @Spy
    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;
    @Spy
    private final TransactionResponseMapper transactionResponseMapper = TransactionResponseMapper.INSTANCE;

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private TransactionHandler transactionHandler;

    @Mock
    private TraceContext traceContext;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private DepositTransactionRepository depositTransactionRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @InjectMocks
    private DepositTransactionService depositTransactionService;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(depositTransactionService, "serviceName", "testService");
        ReflectionTestUtils.setField(depositTransactionService, "transactionResponseTopicName", "testTransactionResponseTopic");
        ReflectionTestUtils.setField(depositTransactionService, "transactionExceptionResponseTopicName", "testTransactionExceptionResponseTopic");
    }

    @Test
    public void successHandleTransactionRequest() {
//        Arrange
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest();

        Mockito.doReturn(Optional.empty())
                .when(depositTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doNothing()
                .when(transactionHandler)
                .commitCompletedTransaction(Mockito.any(DepositTransaction.class));

        Mockito.doReturn(getValidWallet())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doReturn(getValidWallet())
                .when(walletService)
                .getWalletWithLock(Mockito.any(UUID.class));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));

        Mockito.verify(walletService, Mockito.times(1))
                .getWallet(Mockito.any(UUID.class));
        Mockito.verify(walletService, Mockito.times(1))
                .isWalletBlocked(Mockito.any(Wallet.class));
        Mockito.verify(walletService, Mockito.times(1))
                .isWalletCreditCardExpired(Mockito.any(CreditCard.class));

        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitCompletedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(transactionHandler, Mockito.never())
                .commitFailedTransaction(Mockito.any(DepositTransaction.class));

        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnNotIdempotentTransactionRequest() {
//        Arrange
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doThrow(DuplicateTransactionHandleException.class)
                .when(depositTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doNothing()
                .when(transactionHandler)
                .commitFailedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnWalletIsBlocked() {
//        Arrange
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doReturn(Optional.empty())
                .when(depositTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doReturn(getValidWalletBlocked())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doThrow(EntityConstraintsException.class)
                .when(walletService)
                .isWalletBlocked(Mockito.any(Wallet.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnCreditCardWasExpired() {
//        Arrange
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doReturn(Optional.empty())
                .when(depositTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doReturn(getValidWalletBlocked())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doThrow(EntityConstraintsException.class)
                .when(walletService)
                .isWalletCreditCardExpired(Mockito.any(CreditCard.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnWalletNotFound() {
//        Arrange
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doReturn(Optional.empty())
                .when(depositTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doThrow(EntityNotFoundException.class)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnWalletNotFoundWithLockMode() {
//        Arrange
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doReturn(getValidWallet())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doThrow(EntityNotFoundException.class)
                .when(walletService)
                .getWalletWithLock(Mockito.any(UUID.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(DepositTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnErrorWhileAttemptToSendTransactionResponseToKafka() throws ExecutionException, InterruptedException {
//         Arrange
        WalletServiceDepositTransactionRequestDto request = getValidDepositTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doReturn(Optional.empty())
                .when(depositTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doReturn(getValidWalletBlocked())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doThrow(EntityConstraintsException.class)
                .when(walletService)
                .isWalletBlocked(Mockito.any(Wallet.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());
        Mockito.doThrow(ExecutionException.class)
                .when(future)
                .get();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.handleTransactionRequest(request));

        Mockito.verify(kafkaTemplate, Mockito.times(1))
                .send(Mockito.anyString(), Mockito.any(WalletServiceTransactionResponseErrorDto.class));
    }
}
