package aq.project.test.fail_transaction_request_handler.unit;

import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceFailTransactionRequestDto;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.utils.handlers.FailTransactionRequestHandler;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static aq.project._utils.TransactionEntities.getValidDepositTransaction;
import static aq.project._utils.TransactionRequests.getInvalidWalletServiceFailTransactionRequestDto;
import static aq.project._utils.TransactionRequests.getValidWalletServiceFailTransactionRequestDto;

@ExtendWith(MockitoExtension.class)
public class HandleFailDepositTransactionRequestUnitTest {

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private DepositTransactionRepository depositTransactionRepository;

    @InjectMocks
    private FailTransactionRequestHandler failTransactionRequestHandler;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(failTransactionRequestHandler, "serviceName", "testService");
    }

    @Test
    public void successHandleCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceFailTransactionRequestDto failDepositTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        DepositTransaction depositTransaction = getValidDepositTransaction();
        depositTransaction.setStatus(TransactionStatus.PENDING);
        depositTransaction.setProcessed(false);

        Mockito.when(depositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(depositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailDepositTransactionRequest(failDepositTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, depositTransaction.getStatus());
        Assertions.assertTrue(depositTransaction.isProcessed());

        Mockito.verify(depositTransactionRepository, Mockito.times(1))
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInCanceledState() {
//        Arrange
        WalletServiceFailTransactionRequestDto failDepositTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        DepositTransaction depositTransaction = getValidDepositTransaction();
        depositTransaction.setStatus(TransactionStatus.CANCELED);
        depositTransaction.setProcessed(true);

        Mockito.when(depositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(depositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailDepositTransactionRequest(failDepositTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, depositTransaction.getStatus());

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInFailedState() {
//        Arrange
        WalletServiceFailTransactionRequestDto failDepositTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        DepositTransaction depositTransaction = getValidDepositTransaction();
        depositTransaction.setStatus(TransactionStatus.FAILED);
        depositTransaction.setProcessed(true);

        Mockito.when(depositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(depositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailDepositTransactionRequest(failDepositTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, depositTransaction.getStatus());

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionNotFound() {
//        Arrange
        WalletServiceFailTransactionRequestDto failDepositTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        Mockito.when(depositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailDepositTransactionRequest(failDepositTransactionRequestDto));

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleInvalidCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceFailTransactionRequestDto failDepositTransactionRequestDto = getInvalidWalletServiceFailTransactionRequestDto();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailDepositTransactionRequest(failDepositTransactionRequestDto));

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleNullCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceFailTransactionRequestDto failDepositTransactionRequestDto = null;

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailDepositTransactionRequest(failDepositTransactionRequestDto));

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }
}
