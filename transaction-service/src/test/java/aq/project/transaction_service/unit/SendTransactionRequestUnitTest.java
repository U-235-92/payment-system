package aq.project.transaction_service.unit;

import aq.project.entities.Transaction;
import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.ServiceHttpException;
import aq.project.messages.TransactionRequest;
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

import static aq.project._utils.Entities.getValidTransactionRequest;

@ExtendWith(MockitoExtension.class)
public class SendTransactionRequestUnitTest {

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
    public void successSendTransactionRequestUnitTest() {
//        Arrange
        TransactionRequest transactionRequest = getValidTransactionRequest();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.sendTransactionRequest(transactionRequest));

        Mockito.verify(paymentServiceHandler, Mockito.times(1))
                .sendTransactionRequestToPaymentService(transactionRequest);
        Mockito.verify(transactionRepository, Mockito.times(1))
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failSendTransactionRequestOn4xxPaymentServiceHandlerErrorUnitTest() {
//        Arrange
        TransactionRequest transactionRequest = getValidTransactionRequest();

        Mockito.doThrow(ClientHttpException.class)
                .when(paymentServiceHandler)
                .sendTransactionRequestToPaymentService(transactionRequest);

//        Act & Assert
        Assertions.assertThrows(ClientHttpException.class,
                () -> transactionService.sendTransactionRequest(transactionRequest));

        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
    }

    @Test
    public void failSendTransactionRequestOn5xxPaymentServiceHandlerErrorUnitTest() {
//        Arrange
        TransactionRequest transactionRequest = getValidTransactionRequest();

        Mockito.doThrow(ServiceHttpException.class)
                .when(paymentServiceHandler)
                .sendTransactionRequestToPaymentService(transactionRequest);

//        Act & Assert
        Assertions.assertThrows(ServiceHttpException.class,
                () -> transactionService.sendTransactionRequest(transactionRequest));

        Mockito.verify(transactionRepository, Mockito.never())
                .save(Mockito.any(Transaction.class));
    }
}
