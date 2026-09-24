package aq.project.services.transfer_transaction.unit;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.IndividualsApiServiceTransferTransactionRequestDto;
import aq.project.dto.RateResponse;
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

import java.math.BigDecimal;
import java.util.UUID;

import static aq.project._utils.entities.transfer_transaction_service.TransferTransactionServiceEntities.getValidIndividualsApiServiceTransferTransactionRequestDto;
import static aq.project._utils.entities.transfer_transaction_service.TransferTransactionServiceEntities.getValidRateResponse;

@ExtendWith(MockitoExtension.class)
public class CreateTransferTransactionUnitTest {

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
    public void successCreateTransferTransaction() {
//        Arrange
        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceTransferTransactionRequestDto();

        RateResponse rateResponse = getValidRateResponse();

        UUID guessCreatedWalletId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(walletService.getWalletCurrencyCode(Mockito.any(UUID.class)))
                .thenReturn(Mono.just("USD"));
        Mockito.when(currencyRateService.getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(rateResponse));
        Mockito.when(transferTransactionApiClient.createTransferTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(guessCreatedWalletId)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());

        UUID testCreatedWalletId = transferTransactionService.createTransferTransaction(transactionRequestDto).block();

        Assertions.assertEquals(guessCreatedWalletId, testCreatedWalletId);
    }

    @Test
    public void failCreateTransferTransactionOnReceivedInvalidCurrencyCode() {
//        Arrange
        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceTransferTransactionRequestDto();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(walletService.getWalletCurrencyCode(Mockito.any(UUID.class)))
                .thenReturn(Mono.just("INVALID_CURRENCY_CODE"));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());

        Mockito.verify(currencyRateService, Mockito.never())
                .getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any());
        Mockito.verify(transferTransactionApiClient, Mockito.never())
                .createTransferTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class));
    }

    @Test
    public void failCreateTransferTransactionOnInvalidConversionRate() {
//        Arrange
        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceTransferTransactionRequestDto();

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
                () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());

        Mockito.verify(transferTransactionApiClient, Mockito.never())
                .createTransferTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class));
    }

    @Test
    public void failCreateTransferTransactionOnInvalidAmount() {
//        Arrange
        IndividualsApiServiceTransferTransactionRequestDto transactionRequestDto = getValidIndividualsApiServiceTransferTransactionRequestDto();
        transactionRequestDto.setAmount(BigDecimal.valueOf(-100.0));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transferTransactionService.createTransferTransaction(transactionRequestDto).block());

        Mockito.verify(currencyRateService, Mockito.never())
                .getRate(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(), Mockito.any());
        Mockito.verify(transferTransactionApiClient, Mockito.never())
                .createTransferTransaction(Mockito.any(String.class), Mockito.any(Mono.class), Mockito.any(String.class));
    }
}
