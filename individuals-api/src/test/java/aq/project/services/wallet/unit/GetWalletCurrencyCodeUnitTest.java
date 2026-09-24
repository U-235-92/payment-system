package aq.project.services.wallet.unit;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.services.wallets.WalletService;
import aq.project.wallet_service.WalletApiClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class GetWalletCurrencyCodeUnitTest {

    @Mock
    private WalletApiClient walletApiClient;

    @Mock
    private KeycloakServiceClientFacade keycloakServiceClientFacade;

    @InjectMocks
    private WalletService walletService;

    @BeforeEach
    void setUpFields() {
        ReflectionTestUtils.setField(walletService, "serviceName", "service");
    }

    @Test
    public void successGetWalletCurrencyCode() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        Mockito.when(keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue())
                .thenReturn(Mono.just("TEST_JWT"));
        Mockito.when(walletApiClient.getWalletCurrency(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(Mono.just(ResponseEntity.ok("USD")));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletService.getWalletCurrencyCode(walletId).block());

        Mockito.verify(walletApiClient, Mockito.times(1))
                .getWalletCurrency(Mockito.any(UUID.class), Mockito.any(String.class), Mockito.any(String.class));
    }
}
