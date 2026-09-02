package aq.project.test.services.withdraw_transaction_service.unit;

import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.dto.WithdrawTransactionRequestDto;
import aq.project.repositories.transaction.WithdrawTransactionRepository;
import aq.project.services.transaction.WithdrawTransactionService;
import aq.project.services.wallet.WalletService;
import aq.project.utils.handlers.TransactionHandler;
import aq.project.utils.mappers.transaction.TransactionRequestMapper;
import aq.project.utils.mappers.transaction.TransactionResponseMapper;
import aq.project.utils.telemetry.TraceContext;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static aq.project._utils.TransactionRequests.*;
import static aq.project._utils.WalletEntities.*;

@ExtendWith(MockitoExtension.class)
public class HandleTransactionRequestUnitTest {

    @Spy
    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;
    @Spy
    private final TransactionResponseMapper transactionResponseMapper = TransactionResponseMapper.INSTANCE;

    @Spy
    private final OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Mock
    private TransactionHandler transactionHandler;

    @Mock
    private TraceContext traceContext;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private WalletService walletService;

    @Mock
    private WithdrawTransactionRepository withdrawTransactionRepository;

    @InjectMocks
    private WithdrawTransactionService withdrawTransactionService;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(withdrawTransactionService, "tracerName", "testTracer");
        ReflectionTestUtils.setField(withdrawTransactionService, "transactionResponseTopicName", "testTopic");
    }

    @Test
    public void successHandleTransactionRequest() {
        // Arrange & Act
        WithdrawTransactionRequestDto request = getValidWithdrawTransactionRequest();

        Mockito.doNothing()
                .when(transactionHandler)
                .checkWithdrawTransactionPresent(Mockito.any(UUID.class));
        Mockito.doNothing()
                .when(transactionHandler)
                .commitCompletedTransaction(Mockito.any(WithdrawTransaction.class));

        Mockito.doReturn(getValidWallet())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doReturn(getValidWallet())
                .when(walletService)
                .getWalletWithLock(Mockito.any(UUID.class));

        // Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.handleTransactionRequest(request));

        Mockito.verify(walletService, Mockito.times(1))
                .getWallet(Mockito.any(UUID.class));
        Mockito.verify(walletService, Mockito.times(1))
                .isWalletBlocked(Mockito.any(Wallet.class));
        Mockito.verify(walletService, Mockito.times(1))
                .isWalletCreditCardExpired(Mockito.any(CreditCard.class));
        Mockito.verify(walletService, Mockito.times(1))
                .isWalletCreditCardBalanceLessThan(Mockito.any(CreditCard.class), Mockito.any());

        Mockito.verify(transactionHandler, Mockito.times(1))
                .checkWithdrawTransactionPresent(Mockito.any(UUID.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitCompletedTransaction(Mockito.any(WithdrawTransaction.class));
        Mockito.verify(transactionHandler, Mockito.never())
                .commitFailedTransaction(Mockito.any(WithdrawTransaction.class));

        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnNotIdempotentTransactionRequest() {
        // Arrange & Act
        WithdrawTransactionRequestDto request = getValidWithdrawTransactionRequest();

        Mockito.doThrow(DuplicateTransactionHandleException.class)
                .when(transactionHandler)
                .checkWithdrawTransactionPresent(Mockito.any(UUID.class));
        Mockito.doNothing()
                .when(transactionHandler)
                .commitFailedTransaction(Mockito.any(WithdrawTransaction.class));

        // Assert
        Assertions.assertThrows(DuplicateTransactionHandleException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(WithdrawTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(WithdrawTransaction.class));

        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnWalletNotFound() {
        // Arrange & Act
        WithdrawTransactionRequestDto request = getValidWithdrawTransactionRequest();

        Mockito.doNothing()
                .when(transactionHandler)
                .checkWithdrawTransactionPresent(Mockito.any(UUID.class));

        Mockito.doThrow(EntityNotFoundException.class)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));

        // Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(WithdrawTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(WithdrawTransaction.class));

        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnWalletBlocked() {
        // Arrange & Act
        WithdrawTransactionRequestDto request = getValidWithdrawTransactionRequest();

        Mockito.doNothing()
                .when(transactionHandler)
                .checkWithdrawTransactionPresent(Mockito.any(UUID.class));

        Mockito.doReturn(getValidWalletBlocked())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doThrow(EntityConstraintsException.class)
                .when(walletService)
                .isWalletBlocked(Mockito.any(Wallet.class));

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(WithdrawTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(WithdrawTransaction.class));

        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnCreditCardExpired() {
        // Arrange & Act
        WithdrawTransactionRequestDto request = getValidWithdrawTransactionRequest();

        Mockito.doNothing()
                .when(transactionHandler)
                .checkWithdrawTransactionPresent(Mockito.any(UUID.class));

        Wallet wallet = getValidWallet();
        wallet.setCreditCard(getInvalidCreditCardExpired());

        Mockito.doReturn(wallet)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doThrow(EntityConstraintsException.class)
                .when(walletService)
                .isWalletCreditCardExpired(Mockito.any(CreditCard.class));

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(WithdrawTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(WithdrawTransaction.class));

        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnInsufficientBalance() {
        // Arrange & Act
        WithdrawTransactionRequestDto request = getValidWithdrawTransactionRequest();

        Mockito.doNothing()
                .when(transactionHandler)
                .checkWithdrawTransactionPresent(Mockito.any(UUID.class));

        Wallet wallet = getValidWallet();
        wallet.getCreditCard().setBalance(new java.math.BigDecimal("10.00")); // меньше суммы 50.75

        Mockito.doReturn(wallet)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doThrow(EntityConstraintsException.class)
                .when(walletService)
                .isWalletCreditCardBalanceLessThan(Mockito.any(CreditCard.class), Mockito.any());

        // Assert
        Assertions.assertThrows(EntityConstraintsException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(WithdrawTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(WithdrawTransaction.class));

        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnWalletNotFoundWithLockMode() {
        // Arrange & Act
        WithdrawTransactionRequestDto request = getValidWithdrawTransactionRequest();

        Mockito.doReturn(getValidWallet())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doThrow(EntityNotFoundException.class)
                .when(walletService)
                .getWalletWithLock(Mockito.any(UUID.class));

        // Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> withdrawTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(WithdrawTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(WithdrawTransaction.class));

        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }
}