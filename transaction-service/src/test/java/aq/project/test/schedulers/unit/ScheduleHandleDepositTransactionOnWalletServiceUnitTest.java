package aq.project.test.schedulers.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.utils.handlers.wallet_service.request.WalletServiceDepositTransactionRequestHandler;
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

import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidWalletServiceDepositTransactionRequest;

@ExtendWith(MockitoExtension.class)
public class ScheduleHandleDepositTransactionOnWalletServiceUnitTest {

    @Mock
    private WalletServiceDepositTransactionRequestRepository walletServiceDepositTransactionRequestRepository;

    @Mock
    private WalletServiceDepositTransactionRequestHandler walletServiceDepositTransactionRequestHandler;

    @InjectMocks
    private WalletServiceCreateTransactionScheduler walletServiceCreateTransactionScheduler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(
                walletServiceCreateTransactionScheduler, "walletServiceName", "wallet-test-service");
    }

    @Test
    public void successScheduleHandleCreateDepositTransactionOnWalletService() {
//        Arrange
        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setStatus(TransactionStatus.PENDING);

        Mockito.when(walletServiceDepositTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(walletServiceDepositTransactionRequest));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleDepositTransactionOnWalletService());

        Mockito.verify(walletServiceDepositTransactionRequestHandler, Mockito.times(1))
                .handleCreateTransactionRequest(Mockito.any(WalletServiceDepositTransactionRequest.class));
    }

    @Test
    public void successScheduleHandleFailDepositTransactionOnWalletService() {
//        Arrange
        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setStatus(TransactionStatus.FAILED);

        Mockito.when(walletServiceDepositTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(walletServiceDepositTransactionRequest));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleDepositTransactionOnWalletService());

        Mockito.verify(walletServiceDepositTransactionRequestHandler, Mockito.times(1))
                .handleFailTransactionRequest(Mockito.any(WalletServiceDepositTransactionRequest.class));
    }

    @Test
    public void successScheduleHandleFailDepositTransactionOnEmptyList() {
//        Arrange
        Mockito.when(walletServiceDepositTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleDepositTransactionOnWalletService());
    }

    @Test
    public void failScheduleHandleDepositTransactionOnInvalidTransactionStatus() {
//        Arrange
        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = getValidWalletServiceDepositTransactionRequest();
        walletServiceDepositTransactionRequest.setStatus(TransactionStatus.COMPLETED);

        Mockito.when(walletServiceDepositTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(walletServiceDepositTransactionRequest));

//        Act & Assert
        Assertions.assertThrows(ProcessOperationException.class,
                () -> walletServiceCreateTransactionScheduler.scheduleHandleDepositTransactionOnWalletService());
    }
}
