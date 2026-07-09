package aq.project.integration;

import aq.project.dto.ErrorDTO;
import aq.project.exceptions.TransactionException;
import aq.project.services.TransactionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.util.UUID;

@EnableWireMock(@ConfigureWireMock(name = "wallet-service-mock"))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionStatusIntegrationTest {

    @Value("${service.wallet-service.endpoints.get-transaction-status}")
    private String walletServiceGetTransactionStatusEndpoint;

    @Autowired
    private TransactionService transactionService;

    @InjectWireMock("wallet-service-mock")
    private WireMockServer walletServiceMockServer;

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        registry.add("service.wallet-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    public void successGetTransactionStatusUnitTest() {
        String transactionId = UUID.randomUUID().toString();
        walletServiceMockServer.stubFor(WireMock.get(walletServiceGetTransactionStatusEndpoint + "/" + transactionId)
                .willReturn(WireMock.ok()));
        Assertions.assertDoesNotThrow(() -> transactionService.getTransactionStatus(transactionId));
    }

    @Test
    public void failGetTransactionStatusWithUnknownTransactionIdUnitTest() {
        String unknownTransactionId = UUID.randomUUID().toString();

        ErrorDTO errorDto = new ErrorDTO();
        errorDto.setMessage("Unknown transaction ID");
        errorDto.setHttpStatus(400);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(errorDto);

        ResponseDefinition responseDefinition = ResponseDefinitionBuilder.responseDefinition()
                .withStatus(400)
                .withJsonBody(jsonNode)
                .build();

        walletServiceMockServer.stubFor(WireMock.get(walletServiceGetTransactionStatusEndpoint + "/" + unknownTransactionId)
                .willReturn(ResponseDefinitionBuilder.like(responseDefinition)));

        Assertions.assertThrows(TransactionException.class, () -> transactionService
                .getTransactionStatus(unknownTransactionId));
    }

    @Test
    public void failGetTransactionStatusWithInvalidTransactionIdUnitTest() {
        String id = "invalid-id";
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService.getTransactionStatus(id));
    }

    @Test
    public void failGetTransactionStatusWithNullTransactionIdUnitTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService.getTransactionStatus(null));
    }
}
