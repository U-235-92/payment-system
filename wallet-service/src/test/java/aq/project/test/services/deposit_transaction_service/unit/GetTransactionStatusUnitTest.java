package aq.project.test.services.deposit_transaction_service.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.services.transaction.DepositTransactionService;
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

import static aq.project._utils.TransactionEntities.getValidDepositTransaction;

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
    private DepositTransactionRepository depositTransactionRepository;

    @InjectMocks
    private DepositTransactionService depositTransactionService;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(depositTransactionService, "tracerName", "testTracer");
        ReflectionTestUtils.setField(depositTransactionService, "transactionResponseTopicName", "testTopic");
    }

    @Test
    public void successGetTransactionStatus() {
//        Arrange & Act
        DepositTransaction transaction = getValidDepositTransaction();

        Mockito.doReturn(Optional.of(transaction))
                .when(depositTransactionRepository)
                .findById(Mockito.any(UUID.class));

//        Assert
        TransactionStatus status = depositTransactionService.getTransactionStatus(transaction.getId());

        Assertions.assertDoesNotThrow(() -> depositTransactionService.getTransactionStatus(transaction.getId()));
        Assertions.assertEquals(status, transaction.getStatus());
    }

    @Test
    public void failGetTransactionStatusOnNotFoundTransaction() {
//        Arrange & Act
        DepositTransaction transaction = getValidDepositTransaction();

        Mockito.doThrow(EntityNotFoundException.class)
                .when(depositTransactionRepository)
                .findById(Mockito.any(UUID.class));

//        Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> depositTransactionService.getTransactionStatus(transaction.getId()));
    }
}
