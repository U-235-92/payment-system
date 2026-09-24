package aq.project.controllers.transaction;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.TransactionStatus;
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

import static aq.project.controller.DepositTransactionRestControllerApi.PATH_GET_DEPOSIT_TRANSACTION_STATUS;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(name = "transaction-service", port = 18085))
public class GetDepositTransactionStatusWebTest {

    @Value("${application.transaction-service.endpoints.get-deposit-transaction-status}")
    private String transactionServiceGetDepositTransactionStatusEndpoint;

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("transaction-service")
    private WireMockServer transactionServiceMock;

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("application.transaction-service.uri", () -> "http://localhost:18085");
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void successGetDepositTransactionStatus() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetDepositTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetDepositTransactionStatusEndpoint, transactionId);
        String individualsApiServiceGetDepositTransactionStatusEndpoint = PATH_GET_DEPOSIT_TRANSACTION_STATUS
                .replace("{transactionId}", transactionId.toString());

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetDepositTransactionStatusEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(TransactionStatus.COMPLETED)));

//        Act & Assert
        webTestClient.get()
                .uri(individualsApiServiceGetDepositTransactionStatusEndpoint)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @WithMockUser(username = "user", password = "pass")
    public void failGetDepositTransactionStatusOnTransactionService5xxError() {
        //        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetDepositTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetDepositTransactionStatusEndpoint, transactionId);
        String individualsApiServiceGetDepositTransactionStatusEndpoint = PATH_GET_DEPOSIT_TRANSACTION_STATUS
                .replace("{transactionId}", transactionId.toString());

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetDepositTransactionStatusEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        webTestClient.get()
                .uri(individualsApiServiceGetDepositTransactionStatusEndpoint)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is5xxServerError();
    }

    @Test
    public void failGetDepositTransactionStatusOnTransactionService4xxError() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String transactionServiceGetDepositTransactionStatusEndpoint = String.format(
                "%s/%s", this.transactionServiceGetDepositTransactionStatusEndpoint, transactionId);
        String individualsApiServiceGetDepositTransactionStatusEndpoint = PATH_GET_DEPOSIT_TRANSACTION_STATUS
                .replace("{transactionId}", transactionId.toString());

        transactionServiceMock.stubFor(WireMock.get(transactionServiceGetDepositTransactionStatusEndpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        webTestClient.get()
                .uri(individualsApiServiceGetDepositTransactionStatusEndpoint)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    public void failCreateDepositTransactionOnUnauthorizedRequest() {
//        Arrange
        UUID transactionId = UUID.randomUUID();

        String individualsApiServiceGetDepositTransactionStatusEndpoint = PATH_GET_DEPOSIT_TRANSACTION_STATUS
                .replace("{transactionId}", transactionId.toString());

//        Act & Assert
        webTestClient.get()
                .uri(individualsApiServiceGetDepositTransactionStatusEndpoint)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
