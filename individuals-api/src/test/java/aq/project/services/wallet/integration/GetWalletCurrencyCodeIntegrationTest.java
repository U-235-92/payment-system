package aq.project.services.wallet.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.services.wallets.WalletService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "wallet-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWalletCurrencyCodeIntegrationTest {

    @Value("${application.wallet-service.endpoints.get-wallet-currency}")
    private String walletServiceGetWalletCurrencyEndpoint;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;

    @Autowired
    private WalletService walletService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.wallet-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successGetWalletCurrencyCode() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.ok("USD")));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletService.getWalletCurrencyCode(walletId).block());
    }

    @Test
    public void failGetWalletCurrencyCodeOnWalletService4xxErrorResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> walletService.getWalletCurrencyCode(walletId).block());
    }

    @Test
    public void failGetWalletCurrencyCodeOnWalletService5xxErrorResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        String walletServiceGetWalletCurrencyEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletCurrencyEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletCurrencyEndpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> walletService.getWalletCurrencyCode(walletId).block());
    }

    @Test
    public void failGetWalletCurrencyCodeNullWalletId() {
//        Arrange
        UUID walletId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.getWalletCurrencyCode(walletId).block());
    }
}
