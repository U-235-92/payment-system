package aq.project.services.withdraw_transaction.unit;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.IndividualsApiServiceWithdrawTransactionRequestDto;
import aq.project.dto.RateResponse;
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

import java.math.BigDecimal;
import java.util.UUID;

import static aq.project._utils.entities.withdraw_transaction_service.WithdrawTransactionServiceEntities.getValidIndividualsApiServiceWithdrawTransactionRequestDto;
import static aq.project._utils.entities.withdraw_transaction_service.WithdrawTransactionServiceEntities.getValidRateResponse;

@ExtendWith(MockitoExtension.class)
public class CreateWithdrawTransactionUnitTest {

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
    public void successCreateWithdrawTransaction() {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        RateResponse rateResponse = getValidRateResponse();

        UUID guessCreatedWalletId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(walletService.getWalletCurrencyCode(Mockito.any(UUID.class)))
                .thenReturn(Mono.just("USD"));
        Mockito.when(currencyRateService.getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(rateResponse));
        Mockito.when(withdrawTransactionApiClient.createWithdrawTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(guessCreatedWalletId)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());

        UUID testCreatedWalletId = withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block();

        Assertions.assertEquals(guessCreatedWalletId, testCreatedWalletId);
    }

    @Test
    public void failCreateWithdrawTransactionOnReceivedInvalidCurrencyCode() {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(walletService.getWalletCurrencyCode(Mockito.any(UUID.class)))
                .thenReturn(Mono.just("INVALID_CURRENCY_CODE"));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());

        Mockito.verify(currencyRateService, Mockito.never())
                .getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any());
        Mockito.verify(withdrawTransactionApiClient, Mockito.never())
                .createWithdrawTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class));
    }

    @Test
    public void failCreateWithdrawTransactionOnInvalidConversionRate() {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();

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
                () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());

        Mockito.verify(withdrawTransactionApiClient, Mockito.never())
                .createWithdrawTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class));
    }

    @Test
    public void failCreateWithdrawTransactionOnInvalidAmount() {
//        Arrange
        IndividualsApiServiceWithdrawTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceWithdrawTransactionRequestDto();
        transactionRequestDto.setAmount(BigDecimal.valueOf(-100.0));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> withdrawTransactionService.createWithdrawTransaction(transactionRequestDto).block());

        Mockito.verify(currencyRateService, Mockito.never())
                .getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any());
        Mockito.verify(withdrawTransactionApiClient, Mockito.never())
                .createWithdrawTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class));
    }
}
