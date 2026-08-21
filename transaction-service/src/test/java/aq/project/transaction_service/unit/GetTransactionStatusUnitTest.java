package aq.project.transaction_service.unit;

import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.ServiceHttpException;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TransactionService;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.handlers.WalletServiceHandler;
import aq.project.utils.mappers.TransactionRequestMapper;
import aq.project.utils.telemetry.TraceContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class GetTransactionStatusUnitTest {

    @Spy
    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;

    @Mock
    private PaymentServiceHandler paymentServiceHandler;

    @Mock
    private WalletServiceHandler walletServiceHandler;

    @Mock
    private TraceContext traceContext;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void successGetTransactionStatusUnitTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.getTransactionStatus(transactionId));

        Mockito.verify(walletServiceHandler, Mockito.times(1))
                .getTransactionStatus(transactionId);
    }

    @Test
    public void failGetTransactionStatusOn4xxWalletServiceHandlerErrorUnitTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Mockito.doThrow(ClientHttpException.class)
                .when(walletServiceHandler)
                .getTransactionStatus(transactionId);

//        Act & Assert
        Assertions.assertThrows(ClientHttpException.class,
                () -> transactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusOn5xxWalletServiceHandlerErrorUnitTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Mockito.doThrow(ServiceHttpException.class)
                .when(walletServiceHandler)
                .getTransactionStatus(transactionId);

//        Act & Assert
        Assertions.assertThrows(ServiceHttpException.class,
                () -> transactionService.getTransactionStatus(transactionId));
    }
}
