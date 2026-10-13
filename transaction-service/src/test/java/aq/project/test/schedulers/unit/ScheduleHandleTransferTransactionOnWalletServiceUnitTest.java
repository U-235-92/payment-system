package aq.project.test.schedulers.unit;

import aq.project.dto.TransactionStatus;
import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import aq.project.exceptions.ProcessOperationException;
import aq.project.repositories.wallet_service.WalletServiceTransferTransactionRequestRepository;
import aq.project.utils.handlers.wallet_service.request.WalletServiceTransferTransactionRequestHandler;
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

import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidWalletServiceTransferTransactionRequest;

@ExtendWith(MockitoExtension.class)
public class ScheduleHandleTransferTransactionOnWalletServiceUnitTest {

    @Mock
    private WalletServiceTransferTransactionRequestRepository walletServiceTransferTransactionRequestRepository;

    @Mock
    private WalletServiceTransferTransactionRequestHandler walletServiceTransferTransactionRequestHandler;

    @InjectMocks
    private WalletServiceCreateTransactionScheduler walletServiceCreateTransactionScheduler;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(
                walletServiceCreateTransactionScheduler, "walletServiceName", "wallet-test-service");
    }

    @Test
    public void successScheduleHandleCreateTransferTransactionOnWalletService() {
//        Arrange
        WalletServiceTransferTransactionRequest walletServiceTransferTransactionRequest = getValidWalletServiceTransferTransactionRequest();
        walletServiceTransferTransactionRequest.setStatus(TransactionStatus.PENDING);

        Mockito.when(walletServiceTransferTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(walletServiceTransferTransactionRequest));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleTransferTransactionOnWalletService());

        Mockito.verify(walletServiceTransferTransactionRequestHandler, Mockito.times(1))
                .handleCreateTransactionRequest(Mockito.any(WalletServiceTransferTransactionRequest.class));
    }

    @Test
    public void successScheduleHandleFailTransferTransactionOnWalletService() {
//        Arrange
        WalletServiceTransferTransactionRequest walletServiceTransferTransactionRequest = getValidWalletServiceTransferTransactionRequest();
        walletServiceTransferTransactionRequest.setStatus(TransactionStatus.FAILED);

        Mockito.when(walletServiceTransferTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(walletServiceTransferTransactionRequest));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleTransferTransactionOnWalletService());

        Mockito.verify(walletServiceTransferTransactionRequestHandler, Mockito.times(1))
                .handleFailTransactionRequest(Mockito.any(WalletServiceTransferTransactionRequest.class));
    }

    @Test
    public void successScheduleHandleFailTransferTransactionOnEmptyList() {
//        Arrange
        Mockito.when(walletServiceTransferTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceCreateTransactionScheduler.scheduleHandleTransferTransactionOnWalletService());
    }

    @Test
    public void failScheduleHandleTransferTransactionOnInvalidTransactionStatus() {
//        Arrange
        WalletServiceTransferTransactionRequest walletServiceTransferTransactionRequest = getValidWalletServiceTransferTransactionRequest();
        walletServiceTransferTransactionRequest.setStatus(TransactionStatus.COMPLETED);

        Mockito.when(walletServiceTransferTransactionRequestRepository.findUnprocessedRequests())
                .thenReturn(List.of(walletServiceTransferTransactionRequest));

//        Act & Assert
        Assertions.assertThrows(ProcessOperationException.class,
                () -> walletServiceCreateTransactionScheduler.scheduleHandleTransferTransactionOnWalletService());
    }
}
