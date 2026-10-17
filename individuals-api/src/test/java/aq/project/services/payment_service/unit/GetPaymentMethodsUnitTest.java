package aq.project.services.payment_service.unit;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.PaymentServicePaymentMethodResponseDto;
import aq.project.payment_service.PaymentApiClient;
import aq.project.services.payment_service.PaymentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static aq.project._utils.entities.PaymentServiceEntities.*;

@ExtendWith(MockitoExtension.class)
public class GetPaymentMethodsUnitTest {

    @Mock
    private KeycloakServiceClientFacade keycloakServiceClientFacade;

    @Mock
    private PaymentApiClient paymentApiClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    public void successGetPaymentMethods() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        PaymentServicePaymentMethodResponseDto paymentServicePaymentMethodResponseDto = getValidPaymentServicePaymentMethodResponseDto();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(paymentApiClient.getPaymentMethods(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(Flux.just(paymentServicePaymentMethodResponseDto))));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.getPaymentMethods(currencyCode, countryCode).block());
    }
}
