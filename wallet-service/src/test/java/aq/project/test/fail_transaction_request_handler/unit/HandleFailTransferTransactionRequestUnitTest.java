package aq.project.test.fail_transaction_request_handler.unit;

import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceFailTransactionRequestDto;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.repositories.transaction.TransferTransactionRepository;
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

import static aq.project._utils.TransactionEntities.getValidTransferTransaction;
import static aq.project._utils.TransactionRequests.getInvalidWalletServiceFailTransactionRequestDto;
import static aq.project._utils.TransactionRequests.getValidWalletServiceFailTransactionRequestDto;

@ExtendWith(MockitoExtension.class)
public class HandleFailTransferTransactionRequestUnitTest {

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private TransferTransactionRepository transferTransactionRepository;

    @InjectMocks
    private FailTransactionRequestHandler failTransactionRequestHandler;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(failTransactionRequestHandler, "serviceName", "testService");
    }

    @Test
    public void successHandleCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransferTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        TransferTransaction transferTransaction = getValidTransferTransaction();
        transferTransaction.setStatus(TransactionStatus.PENDING);
        transferTransaction.setProcessed(false);

        Mockito.when(transferTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transferTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailTransferTransactionRequest(failTransferTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transferTransaction.getStatus());
        Assertions.assertTrue(transferTransaction.isProcessed());

        Mockito.verify(transferTransactionRepository, Mockito.times(1))
                .save(Mockito.any(TransferTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInCanceledState() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransferTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        TransferTransaction transferTransaction = getValidTransferTransaction();
        transferTransaction.setStatus(TransactionStatus.CANCELED);
        transferTransaction.setProcessed(true);

        Mockito.when(transferTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transferTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailTransferTransactionRequest(failTransferTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, transferTransaction.getStatus());

        Mockito.verify(transferTransactionRepository, Mockito.never())
                .save(Mockito.any(TransferTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInFailedState() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransferTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        TransferTransaction transferTransaction = getValidTransferTransaction();
        transferTransaction.setStatus(TransactionStatus.FAILED);
        transferTransaction.setProcessed(true);

        Mockito.when(transferTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(transferTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailTransferTransactionRequest(failTransferTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, transferTransaction.getStatus());

        Mockito.verify(transferTransactionRepository, Mockito.never())
                .save(Mockito.any(TransferTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionNotFound() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransferTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        Mockito.when(transferTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailTransferTransactionRequest(failTransferTransactionRequestDto));

        Mockito.verify(transferTransactionRepository, Mockito.never())
                .save(Mockito.any(TransferTransaction.class));
    }

    @Test
    public void failHandleInvalidCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransferTransactionRequestDto = getInvalidWalletServiceFailTransactionRequestDto();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailTransferTransactionRequest(failTransferTransactionRequestDto));

        Mockito.verify(transferTransactionRepository, Mockito.never())
                .save(Mockito.any(TransferTransaction.class));
    }

    @Test
    public void failHandleNullCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransferTransactionRequestDto = null;

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailTransferTransactionRequest(failTransferTransactionRequestDto));

        Mockito.verify(transferTransactionRepository, Mockito.never())
                .save(Mockito.any(TransferTransaction.class));
    }
}
