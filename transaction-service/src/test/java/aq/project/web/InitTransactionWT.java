package aq.project.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.InitTransactionRequestDto;
import aq.project.dto.PaymentType;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.dto.WalletStatus;
import aq.project.services.TokenService;
import aq.project.utils.jwt.JwtUtil;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static aq.project.controller.TransactionRestControllerApi.PATH_INIT_TRANSACTION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(value = @ConfigureWireMock(name = "wallet-service", port = 18085))
public class InitTransactionWT {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @InjectWireMock("wallet-service")
    private WireMockServer walletServiceMock;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

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
    @WithMockUser(username = "user", password = "secret")
    public void successInitTransactionWithMockUser() throws Exception {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.DEPOSIT);

        WalletInfoResponseDto walletInfoResponseDto = TransactionServiceEntities.getWalletInfoResponseDto();

        walletInfoResponseDto.setUserId(UUID.randomUUID());
        walletInfoResponseDto.setWalletId(walletId);
        walletInfoResponseDto.setBalance(BigDecimal.valueOf(30.00));
        walletInfoResponseDto.setWalletStatus(WalletStatus.ACTIVE);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String json = objectMapper.writeValueAsString(initTransactionRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_INIT_TRANSACTION)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void successInitTransactionWithAuthorization() throws Exception {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.DEPOSIT);

        WalletInfoResponseDto walletInfoResponseDto = TransactionServiceEntities.getWalletInfoResponseDto();

        walletInfoResponseDto.setUserId(UUID.randomUUID());
        walletInfoResponseDto.setWalletId(walletId);
        walletInfoResponseDto.setBalance(BigDecimal.valueOf(30.00));
        walletInfoResponseDto.setWalletStatus(WalletStatus.ACTIVE);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());

        String json = objectMapper.writeValueAsString(initTransactionRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_INIT_TRANSACTION)
                        .header("Authorization", authorization)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void failureInitTransactionOnUnauthorizedRequest() throws Exception {
//        Arrange
        UUID walletId = UUID.randomUUID();

        InitTransactionRequestDto initTransactionRequestDto = TransactionServiceEntities.getInitTransactionRequestDto();

        initTransactionRequestDto.setWalletId(walletId);
        initTransactionRequestDto.setAmount(BigDecimal.valueOf(50.00));
        initTransactionRequestDto.setTargetWalletId(null);
        initTransactionRequestDto.setTargetWalletConversionRate(null);
        initTransactionRequestDto.setPaymentType(PaymentType.DEPOSIT);

        WalletInfoResponseDto walletInfoResponseDto = TransactionServiceEntities.getWalletInfoResponseDto();

        walletInfoResponseDto.setUserId(UUID.randomUUID());
        walletInfoResponseDto.setWalletId(walletId);
        walletInfoResponseDto.setBalance(BigDecimal.valueOf(30.00));
        walletInfoResponseDto.setWalletStatus(WalletStatus.ACTIVE);

        String walletInfoEndpoint = "/api/v1/wallet/info/{id}".replace("{id}", walletId.toString());

        walletServiceMock.stubFor(WireMock.get(walletInfoEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(walletInfoResponseDto)));

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String json = objectMapper.writeValueAsString(initTransactionRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_INIT_TRANSACTION)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}