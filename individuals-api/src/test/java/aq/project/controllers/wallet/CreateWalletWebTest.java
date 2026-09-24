package aq.project.controllers.wallet;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.CreateWalletRequestDto;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
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

import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getInvalidCreateWalletRequestDto;
import static aq.project._utils.entities.wallet_service.WalletServiceEntities.getValidCreateWalletRequestDto;
import static aq.project.controller.WalletRestControllerApi.PATH_CREATE_WALLET;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EnableWireMock(@ConfigureWireMock(name = "wallet-service", port = 18085))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateWalletWebTest {

    @Value("${application.wallet-service.endpoints.create-wallet}")
    private String walletServiceCreateWalletEndpoint;

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
    public void successCreateWallet() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = getValidCreateWalletRequestDto();

        WireMock.stubFor(WireMock.post(walletServiceCreateWalletEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(UUID.randomUUID().toString())));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WALLET)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createWalletRequestDto)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWalletOnWalletService5xxErrorResponse() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = getValidCreateWalletRequestDto();

        WireMock.stubFor(WireMock.post(walletServiceCreateWalletEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WALLET)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createWalletRequestDto)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWalletOnWalletService4xxErrorResponse() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = getValidCreateWalletRequestDto();

        WireMock.stubFor(WireMock.post(walletServiceCreateWalletEndpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WALLET)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createWalletRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failCreateWalletOnInvalidCreateWalletRequestDto() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = getInvalidCreateWalletRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WALLET)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createWalletRequestDto)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failCreateWalletOnUnauthorizedRequest() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = getValidCreateWalletRequestDto();

//        Act & Assert
        webTestClient.post()
                .uri(PATH_CREATE_WALLET)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createWalletRequestDto)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
