package aq.project.services.wallet.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.wallets.WalletService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
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

import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidWalletInfoResponseDto;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(name = "wallet-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWalletInfoIntegrationTest {

    @Value("${application.wallet-service.endpoints.get-wallet-info}")
    private String walletServiceGetWalletInfoEndpoint;

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
    public void successGetWalletInfo() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        WalletInfoResponseDto walletInfoResponseDto = getValidWalletInfoResponseDto();

        String walletServiceGetWalletInfoEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletInfoEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletService.getWalletInfo(walletId).block());
    }

    @Test
    public void failGetWalletInfoOnWalletService5xxErrorResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        String walletServiceGetWalletInfoEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletInfoEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletInfoEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(HttpServerErrorException.class,
                () -> walletService.getWalletInfo(walletId).block());
    }

    @Test
    public void failGetWalletInfoOnWalletService4xxErrorResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        String walletServiceGetWalletInfoEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletInfoEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletInfoEndpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> walletService.getWalletInfo(walletId).block());
    }

    @Test
    public void failGetWalletInfoOnNullWalletId() {
//        Arrange
        UUID walletId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.getWalletInfo(walletId).block());
    }

    @Test
    public void failGetWalletInfoOnFallback() {
//        Arrange
        final int RATE_LIMIT = 50;

        UUID walletId = UUID.randomUUID();

        WalletInfoResponseDto walletInfoResponseDto = getValidWalletInfoResponseDto();

        String walletServiceGetWalletInfoEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletInfoEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1) {
                new Thread(() -> walletService.getWalletInfo(walletId).block()).start();
            } else {
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> walletService.getWalletInfo(walletId).block());
            }
        }
    }
}
