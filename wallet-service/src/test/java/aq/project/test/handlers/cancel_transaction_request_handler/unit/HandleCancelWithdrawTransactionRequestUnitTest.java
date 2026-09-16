package aq.project.test.handlers.cancel_transaction_request_handler.unit;

import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceCancelWithdrawTransactionRequestDto;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.repositories.transaction.TransferTransactionRepository;
import aq.project.repositories.transaction.WithdrawTransactionRepository;
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

import static aq.project._utils.TransactionEntities.getValidWithdrawTransaction;
import static aq.project._utils.TransactionRequests.getInvalidWalletServiceCancelWithdrawTransactionRequestDto;
import static aq.project._utils.TransactionRequests.getValidWalletServiceCancelWithdrawTransactionRequestDto;

@ExtendWith(MockitoExtension.class)
public class HandleCancelWithdrawTransactionRequestUnitTest {

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private WithdrawTransactionRepository withdrawTransactionRepository;

    @InjectMocks
    private CancelTransactionRequestHandler cancelTransactionRequestHandler;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(cancelTransactionRequestHandler, "serviceName", "testService");
    }

    @Test
    public void successHandleCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceCancelWithdrawTransactionRequestDto withdrawTransactionRequestDto = getValidWalletServiceCancelWithdrawTransactionRequestDto();

        WithdrawTransaction withdrawTransaction = getValidWithdrawTransaction();
        withdrawTransaction.setStatus(TransactionStatus.PENDING);
        withdrawTransaction.setProcessed(false);

        Mockito.when(withdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(withdrawTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelWithdrawTransactionRequest(withdrawTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, withdrawTransaction.getStatus());
        Assertions.assertTrue(withdrawTransaction.isProcessed());

        Mockito.verify(withdrawTransactionRepository, Mockito.times(1))
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInCanceledState() {
//        Arrange
        WalletServiceCancelWithdrawTransactionRequestDto withdrawTransactionRequestDto = getValidWalletServiceCancelWithdrawTransactionRequestDto();

        WithdrawTransaction withdrawTransaction = getValidWithdrawTransaction();
        withdrawTransaction.setStatus(TransactionStatus.CANCELED);
        withdrawTransaction.setProcessed(false);

        Mockito.when(withdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(withdrawTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelWithdrawTransactionRequest(withdrawTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, withdrawTransaction.getStatus());

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInFailedState() {
//        Arrange
        WalletServiceCancelWithdrawTransactionRequestDto withdrawTransactionRequestDto = getValidWalletServiceCancelWithdrawTransactionRequestDto();

        WithdrawTransaction withdrawTransaction = getValidWithdrawTransaction();
        withdrawTransaction.setStatus(TransactionStatus.FAILED);
        withdrawTransaction.setProcessed(false);

        Mockito.when(withdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(withdrawTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelWithdrawTransactionRequest(withdrawTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, withdrawTransaction.getStatus());

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionNotFound() {
//        Arrange
        WalletServiceCancelWithdrawTransactionRequestDto withdrawTransactionRequestDto = getValidWalletServiceCancelWithdrawTransactionRequestDto();

        Mockito.when(withdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelWithdrawTransactionRequest(withdrawTransactionRequestDto));

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleInvalidCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceCancelWithdrawTransactionRequestDto withdrawTransactionRequestDto = getInvalidWalletServiceCancelWithdrawTransactionRequestDto();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelWithdrawTransactionRequest(withdrawTransactionRequestDto));

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleNullCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceCancelWithdrawTransactionRequestDto withdrawTransactionRequestDto = null;

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> cancelTransactionRequestHandler.handleCancelWithdrawTransactionRequest(withdrawTransactionRequestDto));

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }
}
