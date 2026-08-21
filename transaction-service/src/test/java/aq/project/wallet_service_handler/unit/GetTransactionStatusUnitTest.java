package aq.project.wallet_service_handler.unit;

import aq.project.dto.TransactionStatus;
import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.ServiceHttpException;
import aq.project.services.TokenService;
import aq.project.utils.handlers.WalletServiceHandler;
import aq.project.utils.resilence.Fallback;
import aq.project.utils.telemetry.TraceContext;
import aq.project.wallet_service.TransactionApiClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class GetTransactionStatusUnitTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private TraceContext traceContext;

    @Mock
    private TransactionApiClient transactionApiClient;

    @Mock
    private Fallback fallback;

    @InjectMocks
    private WalletServiceHandler walletServiceHandler;

    @Test
    public void successGetTransactionStatusUnitTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Mockito.doReturn(UUID.randomUUID().toString())
                .when(traceContext)
                .getTraceId();
        Mockito.doReturn(UUID.randomUUID().toString())
                .when(tokenService)
                .getAdminJwtAsAuthorizationHeaderValue();
        Mockito.doReturn(ResponseEntity.ok(TransactionStatus.COMPLETED))
                .when(transactionApiClient)
                .getTransactionStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletServiceHandler.getTransactionStatus(transactionId));

        Mockito.verify(transactionApiClient, Mockito.times(1))
                .getTransactionStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void failGetTransactionStatusOn4xxErrorKeycloakServiceUnitTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Mockito.doThrow(ClientHttpException.class)
                .when(tokenService)
                .getAdminJwtAsAuthorizationHeaderValue();

//        Act & Assert
        Assertions.assertThrows(ClientHttpException.class,
                () -> walletServiceHandler.getTransactionStatus(transactionId));

        Mockito.verify(transactionApiClient, Mockito.never())
                .getTransactionStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void failGetTransactionStatusOn5xxErrorKeycloakServiceUnitTest() {
//        Arrange
        String transactionId = UUID.randomUUID().toString();

        Mockito.doThrow(ServiceHttpException.class)
                .when(tokenService)
                .getAdminJwtAsAuthorizationHeaderValue();

//        Act & Assert
        Assertions.assertThrows(ServiceHttpException.class,
                () -> walletServiceHandler.getTransactionStatus(transactionId));

        Mockito.verify(transactionApiClient, Mockito.never())
                .getTransactionStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}
