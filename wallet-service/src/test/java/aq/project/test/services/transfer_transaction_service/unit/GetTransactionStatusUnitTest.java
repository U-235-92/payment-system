package aq.project.test.services.transfer_transaction_service.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.transaction.TransferTransactionRepository;
import aq.project.services.transaction.TransferTransactionService;
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

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.TransactionEntities.getValidTransferTransaction;

@ExtendWith(MockitoExtension.class)
public class GetTransactionStatusUnitTest {

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
    private TransferTransactionRepository transferTransactionRepository;

    @InjectMocks
    private TransferTransactionService transferTransactionService;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(transferTransactionService, "tracerName", "testTracer");
        ReflectionTestUtils.setField(transferTransactionService, "transactionResponseTopicName", "testTopic");
    }

    @Test
    public void successGetTransactionStatus() {
        // Arrange & Act
        TransferTransaction transaction = getValidTransferTransaction();

        Mockito.doReturn(Optional.of(transaction))
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));

        // Assert
        TransactionStatus status = transferTransactionService.getTransactionStatus(transaction.getId());

        Assertions.assertDoesNotThrow(() -> transferTransactionService.getTransactionStatus(transaction.getId()));
        Assertions.assertEquals(status, transaction.getStatus());
    }

    @Test
    public void failGetTransactionStatusOnNotFoundTransaction() {
        // Arrange & Act
        TransferTransaction transaction = getValidTransferTransaction();

        Mockito.doThrow(EntityNotFoundException.class)
                .when(transferTransactionRepository)
                .findById(Mockito.any(UUID.class));

        // Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> transferTransactionService.getTransactionStatus(transaction.getId()));
    }
}