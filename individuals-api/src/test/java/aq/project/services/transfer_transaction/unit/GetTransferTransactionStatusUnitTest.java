package aq.project.services.transfer_transaction.unit;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.TransactionStatus;
import aq.project.services.currency_rate.CurrencyRateService;
import aq.project.services.transactions.TransferTransactionService;
import aq.project.services.wallets.WalletService;
import aq.project.transaction_service.TransferTransactionApiClient;
import aq.project.utils.mappers.TransferTransactionMapper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class GetTransferTransactionStatusUnitTest {

    @Spy
    private TransferTransactionMapper transferTransactionMapper = TransferTransactionMapper.INSTANCE;

    @Mock
    private KeycloakServiceClientFacade keycloakServiceClientFacade;

    @Mock
    private CurrencyRateService currencyRateService;

    @Mock
    private WalletService walletService;

    @Mock
    private TransferTransactionApiClient transferTransactionApiClient;

    @InjectMocks
    private TransferTransactionService transferTransactionService;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(transferTransactionService, "transferTransactionNotificationEndpoint", "/api/v1/transfer-transaction-notification");
        ReflectionTestUtils.setField(transferTransactionService, "serviceName", "service");
    }

    @Test
    public void successGetTransferTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(transferTransactionApiClient.getTransferTransactionStatus(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(TransactionStatus.COMPLETED)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.getTransferTransactionStatus(transactionId).block());

        TransactionStatus transactionStatus = transferTransactionService.getTransferTransactionStatus(transactionId).block();

        Assertions.assertEquals(TransactionStatus.COMPLETED, transactionStatus);
    }

    @Test
    public void failGetTransferTransactionStatusOnBlankAuthorization() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just(""));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transferTransactionService.getTransferTransactionStatus(transactionId).block());
    }
}
