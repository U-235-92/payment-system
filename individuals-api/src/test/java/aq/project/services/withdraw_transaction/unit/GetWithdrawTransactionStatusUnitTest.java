package aq.project.services.withdraw_transaction.unit;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.TransactionStatus;
import aq.project.services.currency_rate.CurrencyRateService;
import aq.project.services.transactions.WithdrawTransactionService;
import aq.project.services.wallets.WalletService;
import aq.project.transaction_service.WithdrawTransactionApiClient;
import aq.project.utils.mappers.WithdrawTransactionMapper;
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
public class GetWithdrawTransactionStatusUnitTest {

    @Spy
    private WithdrawTransactionMapper withdrawTransactionMapper = WithdrawTransactionMapper.INSTANCE;

    @Mock
    private KeycloakServiceClientFacade keycloakServiceClientFacade;

    @Mock
    private CurrencyRateService currencyRateService;

    @Mock
    private WalletService walletService;

    @Mock
    private WithdrawTransactionApiClient withdrawTransactionApiClient;

    @InjectMocks
    private WithdrawTransactionService withdrawTransactionService;

    @BeforeEach
    public void setUpFields() {
        ReflectionTestUtils.setField(withdrawTransactionService, "withdrawTransactionNotificationEndpoint", "/api/v1/withdraw-transaction-notification");
        ReflectionTestUtils.setField(withdrawTransactionService, "serviceName", "service");
    }

    @Test
    public void successGetWithdrawTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(withdrawTransactionApiClient.getWithdrawTransactionStatus(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(TransactionStatus.COMPLETED)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block());

        TransactionStatus transactionStatus = withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block();

        Assertions.assertEquals(TransactionStatus.COMPLETED, transactionStatus);
    }

    @Test
    public void failGetWithdrawTransactionStatusOnBlankAuthorization() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just(""));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> withdrawTransactionService.getWithdrawTransactionStatus(transactionId).block());
    }
}
