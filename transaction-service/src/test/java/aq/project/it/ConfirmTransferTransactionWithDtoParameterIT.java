package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.ConfirmTransactionRequestDto;
import aq.project.dto.ErrorDto;
import aq.project.dto.PaymentType;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.TransactionService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(value = @ConfigureWireMock(name = "wallet-service", port = 18085))
public class ConfirmTransferTransactionWithDtoParameterIT {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);

        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);

        registry.add("app.service.wallet-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successConfirmTransaction() {
//        Arrange
        UUID walletId = UUID.randomUUID();
        UUID targetWalletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(targetWalletId);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.TRANSFER);

        String transferWalletEndpoint = "/api/v1/wallet/transfer";

        walletServiceMock.stubFor(WireMock.post(transferWalletEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(List.of(walletId.toString(), targetWalletId.toString()))));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.confirmTransaction(confirmTransactionRequestDto));
    }

    @Test
    public void failureOnWalletService4xxResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();
        UUID targetWalletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(targetWalletId);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.TRANSFER);

        String transferWalletEndpoint = "/api/v1/wallet/transfer";

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        walletServiceMock.stubFor(WireMock.post(transferWalletEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> transactionService.confirmTransaction(confirmTransactionRequestDto));
    }

    @Test
    public void failureOnWalletService5xxResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();
        UUID targetWalletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(targetWalletId);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.TRANSFER);

        String transferWalletEndpoint = "/api/v1/wallet/transfer";

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        walletServiceMock.stubFor(WireMock.post(transferWalletEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> transactionService.confirmTransaction(confirmTransactionRequestDto));
    }

    @Test
    public void failureOnInvalidRequestDto() {
//        Arrange
        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(null);
        confirmTransactionRequestDto.setTargetWalletId(null);
        confirmTransactionRequestDto.setTargetWalletConversionRate(null);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(-50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.TRANSFER);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.confirmTransaction(confirmTransactionRequestDto));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        UUID walletId = UUID.randomUUID();
        UUID targetWalletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(targetWalletId);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.TRANSFER);

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> transactionService.confirmTransaction(confirmTransactionRequestDto));
    }

    @Test
    public void failureOnRetryLimitExceeded() {
//        Arrange
        UUID walletId = UUID.randomUUID();
        UUID targetWalletId = UUID.randomUUID();

        ConfirmTransactionRequestDto confirmTransactionRequestDto = TransactionServiceEntities
                .getConfirmTransactionRequestDto();

        confirmTransactionRequestDto.setWalletId(walletId);
        confirmTransactionRequestDto.setTargetWalletId(targetWalletId);
        confirmTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        confirmTransactionRequestDto.setPaymentType(PaymentType.TRANSFER);

        String transferWalletEndpoint = "/api/v1/wallet/transfer";

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        walletServiceMock.stubFor(WireMock.post(transferWalletEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        FallbackAssert.assertThrowsRetryLimit(FallbackOperationException.class,
                () -> transactionService.confirmTransaction(confirmTransactionRequestDto));
    }
}