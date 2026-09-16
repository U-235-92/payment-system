package aq.project.test.services.transfer_transaction_service.unit;

import aq.project.dto.WalletServiceTransferTransactionRequestDto;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.transaction.TransferTransactionRepository;
import aq.project.services.transaction.TransferTransactionService;
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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static aq.project._utils.TransactionRequests.getValidTransferTransactionRequest;
import static aq.project._utils.WalletEntities.*;

@ExtendWith(MockitoExtension.class)
public class HandleTransactionRequestUnitTest {

    @Spy
    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;
    @Spy
    private final TransactionResponseMapper transactionResponseMapper = TransactionResponseMapper.INSTANCE;

    @Spy
    private final OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private TransactionHandler transactionHandler;

    @Mock
    private TraceContext traceContext;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private WalletService walletService;

    @Mock
    private TransferTransactionRepository transferTransactionRepository;

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @InjectMocks
    private TransferTransactionService transferTransactionService;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(transferTransactionService, "serviceName", "testService");
        ReflectionTestUtils.setField(transferTransactionService, "transactionResponseTopicName", "testTransactionResponseTopic");
        ReflectionTestUtils.setField(transferTransactionService, "transactionExceptionResponseTopicName", "testTransactionExceptionResponseTopic");
    }

    @Test
    public void successHandleTransactionRequest() {
        // Arrange & Act
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        Mockito.doReturn(Optional.empty())
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doNothing()
                .when(transactionHandler)
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));

        Mockito.doReturn(getValidWallet())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doReturn(getValidWallet())
                .when(walletService)
                .getWalletWithLock(Mockito.any(UUID.class));

        // Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(walletService, Mockito.times(2))
                .getWallet(Mockito.any(UUID.class));
        Mockito.verify(walletService, Mockito.times(2))
                .isWalletBlocked(Mockito.any(Wallet.class));
        Mockito.verify(walletService, Mockito.times(2))
                .isWalletCreditCardExpired(Mockito.any(CreditCard.class));
        Mockito.verify(walletService, Mockito.times(1))
                .isWalletCreditCardBalanceLessThan(Mockito.any(CreditCard.class), Mockito.any());

        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.never())
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));

        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnNotIdempotentTransactionRequest() {
        // Arrange & Act
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doThrow(DuplicateTransactionHandleException.class)
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doNothing()
                .when(transactionHandler)
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

        // Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnSenderWalletNotFound() {
        // Arrange
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doReturn(Optional.empty())
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doThrow(EntityNotFoundException.class)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnRecipientWalletNotFound() {
        // Arrange
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doReturn(Optional.empty())
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));
        // Первый вызов getWallet возвращает sender, второй – выбрасывает исключение
        Mockito.doReturn(getValidWallet())
                .doThrow(EntityNotFoundException.class)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnSenderWalletBlocked() {
        // Arrange
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Wallet blockedWallet = getValidWalletBlocked();
        Wallet nonBlockedWallet = getValidWallet();

        Mockito.doReturn(Optional.empty())
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));
        // Отправитель – заблокирован, получатель – валидный
        Mockito.doReturn(blockedWallet)
                .doReturn(nonBlockedWallet)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        // isWalletBlocked вызывается дважды: для sender и recipient.
        // Для sender выбрасываем исключение, для recipient – возвращаем false.
        Mockito.doThrow(EntityConstraintsException.class)
                .doReturn(false)
                .when(walletService)
                .isWalletBlocked(Mockito.any(Wallet.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnRecipientWalletBlocked() {
        // Arrange
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Wallet blockedWallet = getValidWalletBlocked();
        Wallet nonBlockedWallet = getValidWallet();

        Mockito.doReturn(Optional.empty())
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));
        // Отправитель – валидный, получатель – заблокирован
        Mockito.doReturn(nonBlockedWallet)
                .doReturn(blockedWallet)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        // isWalletBlocked вызывается дважды: для sender (false) и recipient (true)
        Mockito.doReturn(false)
                .doThrow(EntityConstraintsException.class)
                .when(walletService)
                .isWalletBlocked(Mockito.any(Wallet.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnSenderCreditCardExpired() {
        // Arrange
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        CreditCard expiredCard = getInvalidCreditCardExpired();
        Wallet senderWallet = getValidWallet();
        senderWallet.setCreditCard(expiredCard);
        Wallet recipientWallet = getValidWallet();

        Mockito.doReturn(Optional.empty())
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doReturn(senderWallet)
                .doReturn(recipientWallet)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        // Для sender – просрочена, для recipient – нет
        Mockito.doThrow(EntityConstraintsException.class)
                .doReturn(false)
                .when(walletService)
                .isWalletCreditCardExpired(Mockito.any(CreditCard.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnRecipientCreditCardExpired() {
        // Arrange
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Wallet senderWallet = getValidWallet();
        Wallet recipientWallet = getValidWallet();
        recipientWallet.setCreditCard(getInvalidCreditCardExpired());

        Mockito.doReturn(Optional.empty())
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doReturn(senderWallet)
                .doReturn(recipientWallet)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        // Для sender – не просрочена, для recipient – просрочена
        Mockito.doReturn(false)
                .doThrow(EntityConstraintsException.class)
                .when(walletService)
                .isWalletCreditCardExpired(Mockito.any(CreditCard.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnSenderInsufficientBalance() {
        // Arrange
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Wallet senderWallet = getValidWallet();
        senderWallet.getCreditCard().setBalance(new BigDecimal("50.00")); // меньше суммы перевода 200.00

        Wallet recipientWallet = getValidWallet();

        Mockito.doReturn(Optional.empty())
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));
        Mockito.doReturn(senderWallet)
                .doReturn(recipientWallet)
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        // isWalletBlocked – оба не заблокированы
        Mockito.doReturn(false)
                .when(walletService)
                .isWalletBlocked(Mockito.any(Wallet.class));
        // isWalletCreditCardExpired – оба не просрочены
        Mockito.doReturn(false)
                .when(walletService)
                .isWalletCreditCardExpired(Mockito.any(CreditCard.class));
        // isWalletCreditCardBalanceLessThan – для sender выбрасываем исключение
        Mockito.doThrow(EntityConstraintsException.class)
                .when(walletService)
                .isWalletCreditCardBalanceLessThan(Mockito.any(CreditCard.class), Mockito.any());
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnSenderWalletNotFoundWithLockMode() {
        // Arrange
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

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

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }

    @Test
    public void failHandleTransactionRequestOnRecipientWalletNotFoundWithLockMode() {
        // Arrange
        WalletServiceTransferTransactionRequestDto request = getValidTransferTransactionRequest();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        // getWallet возвращает оба кошелька
        Mockito.doReturn(getValidWallet())
                .doReturn(getValidWallet())
                .when(walletService)
                .getWallet(Mockito.any(UUID.class));
        // getWalletWithLock для первого вызова (sender) возвращает валидный, для второго (recipient) выбрасывает исключение
        Mockito.doReturn(getValidWallet())
                .doThrow(EntityNotFoundException.class)
                .when(walletService)
                .getWalletWithLock(Mockito.any(UUID.class));
        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.anyString(), Mockito.any());

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.handleTransactionRequest(request));

        Mockito.verify(transactionHandler, Mockito.never())
                .commitCompletedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(transactionHandler, Mockito.times(1))
                .commitFailedTransaction(Mockito.any(TransferTransaction.class));
        Mockito.verify(traceContext, Mockito.times(2))
                .clean();
        Mockito.verify(traceContext, Mockito.times(1))
                .setTraceId(Mockito.anyString());
    }
}