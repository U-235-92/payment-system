package aq.project.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.PaymentType;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TokenService;
import aq.project.utils.jwt.JwtUtil;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static aq.project.controller.TransactionRestControllerApi.PATH_GET_TRANSACTIONS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionsWT {

    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);

        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void successGetTransactionsWithMockUser() throws Exception {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.DEPOSIT);

        transactionRepository.save(newTransaction);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(get(PATH_GET_TRANSACTIONS)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    @Test
    public void successGetTransactionsWithAuthorization() throws Exception {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.DEPOSIT);

        transactionRepository.save(newTransaction);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String authorization = JwtUtil.toBearerToken(tokenService.getAdminJwt());

//        Act & Assert
        mockMvc.perform(get(PATH_GET_TRANSACTIONS)
                        .header("Authorization", authorization)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    @Test
    public void failureGetTransactionsOnUnauthorizedRequest() throws Exception {
//        Arrange
        Transaction newTransaction = TransactionServiceEntities.getTransaction();

        newTransaction.setTransactionStatus(TransactionStatus.PENDING);
        newTransaction.setPaymentType(PaymentType.DEPOSIT);

        transactionRepository.save(newTransaction);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(get(PATH_GET_TRANSACTIONS)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isUnauthorized());
    }
}