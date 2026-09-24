package aq.project.controllers.wallet;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.WalletInfoResponseDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.UUID;

import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidWalletInfoResponseDto;
import static aq.project.controller.WalletRestControllerApi.PATH_GET_WALLET_INFO;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "wallet-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWalletInfoWebTest {

    @Value("${application.wallet-service.endpoints.get-wallet-info}")
    private String walletServiceGetWalletInfoEndpoint;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.wallet-service.uri", () -> "http://localhost:18085");
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void successGetWalletInfo() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        WalletInfoResponseDto walletInfoResponseDto = getValidWalletInfoResponseDto();

        String individualsApiServiceWalletInfoEndpoint = PATH_GET_WALLET_INFO.replace(
                "{walletId}", walletId.toString());
        String walletServiceGetWalletInfoEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletInfoEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

//        Act & Assert
        webTestClient.get()
                .uri(individualsApiServiceWalletInfoEndpoint)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(WalletInfoResponseDto.class);
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failGetWalletInfoOnWalletService5xxErrorResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        String individualsApiServiceWalletInfoEndpoint = PATH_GET_WALLET_INFO.replace(
                "{walletId}", walletId.toString());
        String walletServiceGetWalletInfoEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletInfoEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletInfoEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        webTestClient.get()
                .uri(individualsApiServiceWalletInfoEndpoint)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failGetWalletInfoOnWalletService4xxErrorResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        String individualsApiServiceWalletInfoEndpoint = PATH_GET_WALLET_INFO.replace(
                "{walletId}", walletId.toString());
        String walletServiceGetWalletInfoEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletInfoEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletInfoEndpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        webTestClient.get()
                .uri(individualsApiServiceWalletInfoEndpoint)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failGetWalletInfoOnUnauthorizedRequest() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        WalletInfoResponseDto walletInfoResponseDto = getValidWalletInfoResponseDto();

        String individualsApiServiceWalletInfoEndpoint = PATH_GET_WALLET_INFO.replace(
                "{walletId}", walletId.toString());
        String walletServiceGetWalletInfoEndpoint = String.format(
                "%s/%s", this.walletServiceGetWalletInfoEndpoint, walletId);

        WireMock.stubFor(WireMock.get(walletServiceGetWalletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

//        Act & Assert
        webTestClient.get()
                .uri(individualsApiServiceWalletInfoEndpoint)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
