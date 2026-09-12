package aq.project.test.services.withdraw_transaction_service.unit;

import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.repositories.transaction.WithdrawTransactionRepository;
import aq.project.services.transaction.WithdrawTransactionService;
import aq.project.services.wallet.WalletService;
import aq.project.utils.handlers.TransactionHandler;
import aq.project.utils.mappers.transaction.TransactionRequestMapper;
import aq.project.utils.mappers.transaction.TransactionResponseMapper;
import aq.project.utils.telemetry.TraceContext;
import io.opentelemetry.api.OpenTelemetry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static aq.project._utils.TransactionEntities.getValidWithdrawTransaction;

@ExtendWith(MockitoExtension.class)
public class HandleTransactionUnitTest {

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
    public void successHandleTransaction() {
        // Arrange & Act
        WithdrawTransaction transaction = getValidWithdrawTransaction();

        Mockito.doReturn(Mockito.mock(CompletableFuture.class))
                .when(kafkaTemplate)
                .send(Mockito.any(ProducerRecord.class));

        Mockito.doReturn(new PageImpl<>(List.of(transaction)))
                .when(withdrawTransactionRepository)
                .findAll(Mockito.any(Pageable.class));

        // Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.handleTransaction());
        Assertions.assertTrue(transaction.isProcessed());

        Mockito.verify(kafkaTemplate, Mockito.times(1)).send(Mockito.any(ProducerRecord.class));
    }

    @Test
    public void successHandleTransactionOnEmptyPage() {
        // Arrange & Act
        Mockito.doReturn(Page.empty())
                .when(withdrawTransactionRepository)
                .findAll(Mockito.any(Pageable.class));

        // Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.handleTransaction());

        Mockito.verify(kafkaTemplate, Mockito.never()).send(Mockito.any(ProducerRecord.class));
    }

    @Test
    public void failHandleTransactionOnExceptionAfterGetKafkaResponse() throws ExecutionException, InterruptedException {
        // Arrange & Act
        WithdrawTransaction transaction = getValidWithdrawTransaction();

        CompletableFuture<SendResult<String, Object>> future = Mockito.mock(CompletableFuture.class);

        Mockito.doReturn(future)
                .when(kafkaTemplate)
                .send(Mockito.any(ProducerRecord.class));

        Mockito.doThrow(InterruptedException.class)
                .when(future)
                .get();

        Mockito.doReturn(new PageImpl<>(List.of(transaction)))
                .when(withdrawTransactionRepository)
                .findAll(Mockito.any(Pageable.class));

        // Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.handleTransaction());
        Assertions.assertFalse(transaction.isProcessed());
    }
}