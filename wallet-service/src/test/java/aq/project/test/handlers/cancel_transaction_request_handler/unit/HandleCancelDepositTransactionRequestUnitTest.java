package aq.project.test.handlers.cancel_transaction_request_handler.unit;

import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceCancelTransactionRequestDto;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.utils.handlers.CancelTransactionRequestHandler;
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
import static aq.project._utils.TransactionRequests.getInvalidWalletServiceCancelTransactionRequestDto;
import static aq.project._utils.TransactionRequests.getValidWalletServiceCancelTransactionRequestDto;

@ExtendWith(MockitoExtension.class)
public class HandleCancelDepositTransactionRequestUnitTest {

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private DepositTransactionRepository depositTransactionRepository;

    @InjectMocks
    private CancelTransactionRequestHandler cancelTransactionRequestHandler;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(cancelTransactionRequestHandler, "serviceName", "testService");
    }

    @Test
    public void successHandleCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceCancelTransactionRequestDto cancelTransactionRequestDto = getValidWalletServiceCancelTransactionRequestDto();

        DepositTransaction depositTransaction = getValidDepositTransaction();
        depositTransaction.setStatus(TransactionStatus.PENDING);
        depositTransaction.setProcessed(false);

        Mockito.when(depositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(depositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelDepositTransactionRequest(cancelTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, depositTransaction.getStatus());
        Assertions.assertTrue(depositTransaction.isProcessed());

        Mockito.verify(depositTransactionRepository, Mockito.times(1))
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInCanceledState() {
//        Arrange
        WalletServiceCancelTransactionRequestDto cancelTransactionRequestDto = getValidWalletServiceCancelTransactionRequestDto();

        DepositTransaction depositTransaction = getValidDepositTransaction();
        depositTransaction.setStatus(TransactionStatus.CANCELED);
        depositTransaction.setProcessed(true);

        Mockito.when(depositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(depositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelDepositTransactionRequest(cancelTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, depositTransaction.getStatus());

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInFailedState() {
//        Arrange
        WalletServiceCancelTransactionRequestDto cancelTransactionRequestDto = getValidWalletServiceCancelTransactionRequestDto();

        DepositTransaction depositTransaction = getValidDepositTransaction();
        depositTransaction.setStatus(TransactionStatus.FAILED);
        depositTransaction.setProcessed(true);

        Mockito.when(depositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(depositTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelDepositTransactionRequest(cancelTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, depositTransaction.getStatus());

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionNotFound() {
//        Arrange
        WalletServiceCancelTransactionRequestDto cancelTransactionRequestDto = getValidWalletServiceCancelTransactionRequestDto();

        Mockito.when(depositTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelDepositTransactionRequest(cancelTransactionRequestDto));

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleInvalidCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceCancelTransactionRequestDto cancelDepositTransactionRequestDto = getInvalidWalletServiceCancelTransactionRequestDto();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelDepositTransactionRequest(cancelDepositTransactionRequestDto));

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }

    @Test
    public void failHandleNullCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceCancelTransactionRequestDto cancelTransactionRequestDto = null;

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelDepositTransactionRequest(cancelTransactionRequestDto));

        Mockito.verify(depositTransactionRepository, Mockito.never())
                .save(Mockito.any(DepositTransaction.class));
    }
}
