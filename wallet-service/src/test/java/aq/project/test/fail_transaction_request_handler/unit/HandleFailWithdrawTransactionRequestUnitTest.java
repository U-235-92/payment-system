package aq.project.test.fail_transaction_request_handler.unit;

import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceFailTransactionRequestDto;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.repositories.transaction.WithdrawTransactionRepository;
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

import static aq.project._utils.TransactionEntities.getValidWithdrawTransaction;
import static aq.project._utils.TransactionRequests.getInvalidWalletServiceFailTransactionRequestDto;
import static aq.project._utils.TransactionRequests.getValidWalletServiceFailTransactionRequestDto;

@ExtendWith(MockitoExtension.class)
public class HandleFailWithdrawTransactionRequestUnitTest {

    @Spy
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

    @Spy
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private ApplicationMetricsRegistry applicationMetricsRegistry;

    @Mock
    private WithdrawTransactionRepository withdrawTransactionRepository;

    @InjectMocks
    private FailTransactionRequestHandler failTransactionRequestHandler;

    @BeforeEach
    public void setUpValueFields() {
        ReflectionTestUtils.setField(failTransactionRequestHandler, "serviceName", "testService");
    }

    @Test
    public void successHandleCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        WithdrawTransaction withdrawTransaction = getValidWithdrawTransaction();
        withdrawTransaction.setStatus(TransactionStatus.PENDING);
        withdrawTransaction.setProcessed(false);

        Mockito.when(withdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(withdrawTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailWithdrawTransactionRequest(failTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, withdrawTransaction.getStatus());
        Assertions.assertTrue(withdrawTransaction.isProcessed());

        Mockito.verify(withdrawTransactionRepository, Mockito.times(1))
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInCanceledState() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        WithdrawTransaction withdrawTransaction = getValidWithdrawTransaction();
        withdrawTransaction.setStatus(TransactionStatus.CANCELED);
        withdrawTransaction.setProcessed(false);

        Mockito.when(withdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(withdrawTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailWithdrawTransactionRequest(failTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.CANCELED, withdrawTransaction.getStatus());

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionInFailedState() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        WithdrawTransaction withdrawTransaction = getValidWithdrawTransaction();
        withdrawTransaction.setStatus(TransactionStatus.FAILED);
        withdrawTransaction.setProcessed(false);

        Mockito.when(withdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(withdrawTransaction));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailWithdrawTransactionRequest(failTransactionRequestDto));
        Assertions.assertEquals(TransactionStatus.FAILED, withdrawTransaction.getStatus());

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleCancelDepositTransactionRequestWhenTransactionNotFound() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransactionRequestDto = getValidWalletServiceFailTransactionRequestDto();

        Mockito.when(withdrawTransactionRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailWithdrawTransactionRequest(failTransactionRequestDto));

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleInvalidCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransactionRequestDto = getInvalidWalletServiceFailTransactionRequestDto();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailWithdrawTransactionRequest(failTransactionRequestDto));

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }

    @Test
    public void failHandleNullCancelDepositTransactionRequest() {
//        Arrange
        WalletServiceFailTransactionRequestDto failTransactionRequestDto = null;

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> failTransactionRequestHandler.handleFailWithdrawTransactionRequest(failTransactionRequestDto));

        Mockito.verify(withdrawTransactionRepository, Mockito.never())
                .save(Mockito.any(WithdrawTransaction.class));
    }
}
