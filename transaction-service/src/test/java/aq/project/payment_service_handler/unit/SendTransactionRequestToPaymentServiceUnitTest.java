package aq.project.payment_service_handler.unit;

import aq.project.dto.TransactionRequestDto;
import aq.project.messages.TransactionRequest;
import aq.project.payment_provider_service.TransactionApiClient;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.mappers.TransactionRequestMapper;
import aq.project.utils.resilence.Fallback;
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

import static aq.project._utils.Entities.getValidTransactionRequest;

@ExtendWith(MockitoExtension.class)
public class SendTransactionRequestToPaymentServiceUnitTest {

    @Spy
    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;

    @Mock
    private TransactionApiClient paymentServiceTransactionApiClient;

    @Mock
    private TraceContext traceContext;

    @Mock
    private Fallback fallback;

    @InjectMocks
    private PaymentServiceHandler paymentServiceHandler;

    @Test
    public void successSendTransactionRequestToPaymentService() {
//        Arrange
        TransactionRequest transactionRequest = getValidTransactionRequest();

        Mockito.doReturn(UUID.randomUUID().toString()).when(traceContext).getTraceId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentServiceHandler.sendTransactionRequestToPaymentService(transactionRequest));

        Mockito.verify(paymentServiceTransactionApiClient, Mockito.times(1))
                .createTransaction(Mockito.anyString(), Mockito.any(TransactionRequestDto.class), Mockito.anyString());
    }
}
