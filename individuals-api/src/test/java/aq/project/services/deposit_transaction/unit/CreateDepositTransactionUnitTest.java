package aq.project.services.deposit_transaction.unit;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.IndividualsApiServiceDepositTransactionRequestDto;
import aq.project.dto.RateResponse;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static aq.project._utils.entities.deposit_transaction_service.DepositTransactionServiceEntities.getValidIndividualsApiServiceDepositTransactionRequestDto;
import static aq.project._utils.entities.deposit_transaction_service.DepositTransactionServiceEntities.getValidRateResponse;

@ExtendWith(MockitoExtension.class)
public class CreateDepositTransactionUnitTest {

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
    public void successCreateDepositTransaction() {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        RateResponse rateResponse = getValidRateResponse();

        UUID guessCreatedWalletId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(walletService.getWalletCurrencyCode(Mockito.any(UUID.class)))
                .thenReturn(Mono.just("USD"));
        Mockito.when(currencyRateService.getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(rateResponse));
        Mockito.when(depositTransactionApiClient.createDepositTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(guessCreatedWalletId)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());

        UUID testCreatedWalletId = depositTransactionService.createDepositTransaction(transactionRequestDto).block();

        Assertions.assertEquals(guessCreatedWalletId, testCreatedWalletId);
    }

    @Test
    public void failCreateDepositTransactionOnReceivedInvalidCurrencyCode() {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(walletService.getWalletCurrencyCode(Mockito.any(UUID.class)))
                .thenReturn(Mono.just("INVALID_CURRENCY_CODE"));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());

        Mockito.verify(currencyRateService, Mockito.never())
                .getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any());
        Mockito.verify(depositTransactionApiClient, Mockito.never())
                .createDepositTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class));
    }

    @Test
    public void failCreateDepositTransactionOnInvalidConversionRate() {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();

        RateResponse rateResponse = getValidRateResponse();
        rateResponse.setRate(BigDecimal.valueOf(-1.0));

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(walletService.getWalletCurrencyCode(Mockito.any(UUID.class)))
                .thenReturn(Mono.just("USD"));
        Mockito.when(currencyRateService.getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(rateResponse));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());

        Mockito.verify(depositTransactionApiClient, Mockito.never())
                .createDepositTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class));
    }

    @Test
    public void failCreateDepositTransactionOnInvalidAmount() {
//        Arrange
        IndividualsApiServiceDepositTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceDepositTransactionRequestDto();
        transactionRequestDto.setAmount(BigDecimal.valueOf(-100.0));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> depositTransactionService.createDepositTransaction(transactionRequestDto).block());

        Mockito.verify(currencyRateService, Mockito.never())
                .getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any());
        Mockito.verify(depositTransactionApiClient, Mockito.never())
                .createDepositTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class));
    }
}
