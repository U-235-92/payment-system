package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.*;
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
import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(value = @ConfigureWireMock(name = "wallet-service", port = 18085))
public class InitWithdrawalTransactionIT {

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
    public void successInitTransactionOnWalletService2xxResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

        WalletInfoResponseDto walletInfoResponseDto = TransactionServiceEntities.getWalletInfoResponseDto();

        walletInfoResponseDto.setUserId(UUID.randomUUID());
        walletInfoResponseDto.setWalletId(walletId);
        walletInfoResponseDto.setBalance(BigDecimal.valueOf(30.00));
        walletInfoResponseDto.setWalletStatus(WalletStatus.ACTIVE);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.initTransaction(initTransactionRequestDto));
    }

    @Test
    public void successInitTransactionOnWalletService2xxResponseAndWalletIsBlocked() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

        WalletInfoResponseDto walletInfoResponseDto = TransactionServiceEntities.getWalletInfoResponseDto();

        walletInfoResponseDto.setUserId(UUID.randomUUID());
        walletInfoResponseDto.setWalletId(walletId);
        walletInfoResponseDto.setBalance(BigDecimal.valueOf(30.00));
        walletInfoResponseDto.setWalletStatus(WalletStatus.BLOCKED);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.initTransaction(initTransactionRequestDto));
    }

    @Test
    public void successInitTransactionOnWalletService2xxResponseAndWalletBalanceLessThanAmount() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(30.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

        WalletInfoResponseDto walletInfoResponseDto = TransactionServiceEntities.getWalletInfoResponseDto();

        walletInfoResponseDto.setUserId(UUID.randomUUID());
        walletInfoResponseDto.setWalletId(walletId);
        walletInfoResponseDto.setBalance(BigDecimal.valueOf(50.00));
        walletInfoResponseDto.setWalletStatus(WalletStatus.ACTIVE);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.initTransaction(initTransactionRequestDto));
    }

    @Test
    public void failureInitTransactionOnWalletService4xxResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> transactionService.initTransaction(initTransactionRequestDto));
    }

    @Test
    public void failureInitTransactionOnWalletService5xxResponse() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> transactionService.initTransaction(initTransactionRequestDto));
    }

    @Test
    public void failureInitTransactionOnInvalidRequestDto() {
//        Arrange
        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(null);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(-50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> transactionService.initTransaction(initTransactionRequestDto));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

        WalletInfoResponseDto walletInfoResponseDto = TransactionServiceEntities.getWalletInfoResponseDto();

        walletInfoResponseDto.setUserId(UUID.randomUUID());
        walletInfoResponseDto.setWalletId(walletId);
        walletInfoResponseDto.setBalance(BigDecimal.valueOf(30.00));
        walletInfoResponseDto.setWalletStatus(WalletStatus.ACTIVE);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> transactionService.initTransaction(initTransactionRequestDto));
    }

    @Test
    public void failureOnRetryExceeded() {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.WITHDRAWAL);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        FallbackAssert.assertThrowsRetryLimit(FallbackOperationException.class,
                () -> transactionService.initTransaction(initTransactionRequestDto));
    }
}