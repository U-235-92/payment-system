package aq.project.services.deposit_transaction.unit;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.TransactionStatus;
import aq.project.services.currency_rate.CurrencyRateService;
import aq.project.services.transactions.DepositTransactionService;
import aq.project.services.wallets.WalletService;
import aq.project.transaction_service.DepositTransactionApiClient;
import aq.project.utils.mappers.DepositTransactionMapper;
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
public class GetDepositTransactionStatusUnitTest {

    @Spy
    private DepositTransactionMapper depositTransactionMapper = DepositTransactionMapper.INSTANCE;

    @Mock
    private KeycloakServiceClientFacade keycloakServiceClientFacade;

    @Mock
    private CurrencyRateService currencyRateService;

    @Mock
    private WalletService walletService;

    @Mock
    private DepositTransactionApiClient depositTransactionApiClient;

    @InjectMocks
    private DepositTransactionService depositTransactionService;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(depositTransactionService, "depositTransactionNotificationEndpoint", "/api/v1/deposit-transaction-notification");
        ReflectionTestUtils.setField(depositTransactionService, "serviceName", "service");
    }

    @Test
    public void successGetDepositTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(depositTransactionApiClient.getDepositTransactionStatus(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(TransactionStatus.COMPLETED)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.getDepositTransactionStatus(transactionId).block());

        TransactionStatus transactionStatus = depositTransactionService.getDepositTransactionStatus(transactionId).block();

        Assertions.assertEquals(TransactionStatus.COMPLETED, transactionStatus);
    }

    @Test
    public void failGetDepositTransactionStatusOnBlankAuthorization() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just(""));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.getDepositTransactionStatus(transactionId).block());
    }
}
