package aq.project.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.MerchantEntities;
import aq.project._utils.entities.PayoutServiceEntities;
import aq.project.dto.CreatePayoutRequestDto;
import aq.project.entities.Merchant;
import aq.project.repositories.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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

import static aq.project.controller.TransactionRestControllerApi.PATH_CREATE_TRANSACTION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePayoutWT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MerchantRepository merchantRepository;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    public void successCreate() throws Exception {
//        Arrange
        Merchant merchant = MerchantEntities.getMerchant();

        merchantRepository.save(merchant);

        String merchantId = merchant.getMerchantId();
        String merchantSecret = merchant.getSecretKey();

        CreatePayoutRequestDto createPayoutRequestDto = PayoutServiceEntities.getCreatePayoutRequestDto();
        createPayoutRequestDto.setMerchantId(merchantId);

        String authorization = getAuthorization(merchantId, merchantSecret);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String json = objectMapper.writeValueAsString(createPayoutRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_TRANSACTION)
                        .header("Authorization", authorization)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
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

        CreatePayoutRequestDto createPayoutRequestDto = PayoutServiceEntities.getCreatePayoutRequestDto();
        createPayoutRequestDto.setMerchantId(merchantId);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String json = objectMapper.writeValueAsString(createPayoutRequestDto);

//        Act & Assert
        mockMvc.perform(post(PATH_CREATE_TRANSACTION)
                        .header(xTraceIdKey, xTraceIdValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}