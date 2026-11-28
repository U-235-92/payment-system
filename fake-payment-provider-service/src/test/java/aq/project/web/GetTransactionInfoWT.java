package aq.project.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.MerchantEntities;
import aq.project._utils.entities.TransactionServiceEntities;
import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.Status;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.mappers.TransactionMapper;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.UUID;

import static aq.project.controller.TransactionRestControllerApi.PATH_GET_TRANSACTION_INFO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionInfoWT {

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    public void successGetPayoutInfo() throws Exception {
//        Arrange
        Merchant merchant = MerchantEntities.getMerchant();

        merchantRepository.save(merchant);

        String merchantId = merchant.getMerchantId();
        String merchantSecret = merchant.getSecretKey();

        CreateTransactionRequestDto createTransactionRequestDto = TransactionServiceEntities
                .getCreateTransactionRequestDto();
        createTransactionRequestDto.setMerchantId(merchantId);

        Transaction newTransaction = transactionMapper.toTransaction(createTransactionRequestDto);

        newTransaction.setMerchant(merchant);
        newTransaction.setStatus(Status.PENDING);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String authorization = getAuthorization(merchantId, merchantSecret);

//        Act & Assert
        mockMvc.perform(get(PATH_GET_TRANSACTION_INFO.replace("{id}", transactionId.toString()))
                        .header("Authorization", authorization)
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    private String getAuthorization(String merchantId, String merchantSecret) {
        String keyPass = String.format("%s:%s", merchantId, merchantSecret);
        String base64KeyPass = Base64.getEncoder().encodeToString(keyPass.getBytes());
        return String.format("Basic %s", base64KeyPass);
    }

    @Test
    public void failureOnUnauthorizedRequest() throws Exception {
//        Arrange
        Merchant merchant = MerchantEntities.getMerchant();

        merchantRepository.save(merchant);

        String merchantId = merchant.getMerchantId();

        CreateTransactionRequestDto createTransactionRequestDto = TransactionServiceEntities
                .getCreateTransactionRequestDto();
        createTransactionRequestDto.setMerchantId(merchantId);

        Transaction newTransaction = transactionMapper.toTransaction(createTransactionRequestDto);

        newTransaction.setMerchant(merchant);
        newTransaction.setStatus(Status.PENDING);

        UUID transactionId = transactionRepository.save(newTransaction).getId();

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(get(PATH_GET_TRANSACTION_INFO.replace("{id}", transactionId.toString()))
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isUnauthorized());
    }
}