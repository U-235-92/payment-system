package aq.project.services.transaction_services.transfer.unit;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.IndividualsApiServiceTransferTransactionRequestDto;
import aq.project.dto.PaymentServiceCreatePaymentResponseDto;
import aq.project.dto.RateResponse;
import aq.project.payment_service.PaymentApiClient;
import aq.project.services.currency_rate_service.CurrencyRateService;
import aq.project.services.transaction_service.TransferTransactionService;
import aq.project.services.wallet_service.WalletService;
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

import static aq.project._utils.entities.PaymentServiceEntities.getValidPaymentServiceCreatePaymentResponseDto;
import static aq.project._utils.entities.TransferTransactionServiceEntities.getValidIndividualsApiServiceTransferTransactionRequestDto;
import static aq.project._utils.entities.TransferTransactionServiceEntities.getValidRateResponse;

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
    @Mock
    private PaymentApiClient paymentApiClient;

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

        PaymentServiceCreatePaymentResponseDto paymentServiceCreatePaymentResponseDto = getValidPaymentServiceCreatePaymentResponseDto();

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
        Mockito.when(paymentApiClient.createPayment(Mockito.any(Mono.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(paymentServiceCreatePaymentResponseDto)));

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
        Mockito.verify(paymentApiClient, Mockito.never())
                .createPayment(Mockito.any(Mono.class), Mockito.any(String.class), Mockito.any(String.class));
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
        Mockito.verify(paymentApiClient, Mockito.never())
                .createPayment(Mockito.any(Mono.class), Mockito.any(String.class), Mockito.any(String.class));
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
        Mockito.verify(paymentApiClient, Mockito.never())
                .createPayment(Mockito.any(Mono.class), Mockito.any(String.class), Mockito.any(String.class));
    }
}
