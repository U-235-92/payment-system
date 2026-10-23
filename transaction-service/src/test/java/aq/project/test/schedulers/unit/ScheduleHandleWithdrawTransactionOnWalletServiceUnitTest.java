package aq.project.test.schedulers.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.wallet_service.WalletServiceWithdrawTransactionRequestRepository;
import aq.project.utils.handlers.wallet_service.request.WalletServiceWithdrawTransactionRequestHandler;
import aq.project.utils.schedulers.WalletServiceCreateTransactionScheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;

import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidWalletServiceWithdrawTransactionRequest;

@ExtendWith(MockitoExtension.class)
public class ScheduleHandleWithdrawTransactionOnWalletServiceUnitTest {

    @Mock
    private WalletServiceWithdrawTransactionRequestRepository walletServiceWithdrawTransactionRequestRepository;

    @Mock
    private WalletServiceWithdrawTransactionRequestHandler walletServiceWithdrawTransactionRequestHandler;

    @InjectMocks
    private WalletServiceCreateTransactionScheduler walletServiceCreateTransactionScheduler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(
                walletServiceCreateTransactionScheduler, "walletServiceName", "wallet-test-service");
    }

    @Test
    public void successScheduleHandleCreateWithdrawTransactionOnWalletService() {
//        Arrange
        WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest = getValidWalletServiceWithdrawTransactionRequest();
        walletServiceWithdrawTransactionRequest.setStatus(TransactionStatus.PENDING);

        Mockito.when(walletServiceWithdrawTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(walletServiceWithdrawTransactionRequest));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleWithdrawTransactionOnWalletService());

        Mockito.verify(walletServiceWithdrawTransactionRequestHandler, Mockito.times(1))
                .handleCreateTransactionRequest(Mockito.any(WalletServiceWithdrawTransactionRequest.class));
    }

    @Test
    public void successScheduleHandleFailWithdrawTransactionOnWalletService() {
//        Arrange
        WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest = getValidWalletServiceWithdrawTransactionRequest();
        walletServiceWithdrawTransactionRequest.setStatus(TransactionStatus.FAILED);

        Mockito.when(walletServiceWithdrawTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(walletServiceWithdrawTransactionRequest));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleWithdrawTransactionOnWalletService());

        Mockito.verify(walletServiceWithdrawTransactionRequestHandler, Mockito.times(1))
                .handleFailTransactionRequest(Mockito.any(WalletServiceWithdrawTransactionRequest.class));
    }

    @Test
    public void successScheduleHandleFailWithdrawTransactionOnEmptyList() {
//        Arrange
        Mockito.when(walletServiceWithdrawTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleWithdrawTransactionOnWalletService());
    }

    @Test
    public void failScheduleHandleWithdrawTransactionOnInvalidTransactionStatus() {
//        Arrange
        WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest = getValidWalletServiceWithdrawTransactionRequest();
        walletServiceWithdrawTransactionRequest.setStatus(TransactionStatus.COMPLETED);

        Mockito.when(walletServiceWithdrawTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(walletServiceWithdrawTransactionRequest));

//        Act & Assert
        Assertions.assertThrows(ProcessOperationException.class,
                () -> walletServiceCreateTransactionScheduler.scheduleHandleWithdrawTransactionOnWalletService());
    }
}
