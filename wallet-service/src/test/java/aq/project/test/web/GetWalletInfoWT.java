package aq.project.test.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.WalletServiceEntities;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.services.WalletService;
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

import static aq.project.controller.WalletRestControllerApi.PATH_GET_WALLET_INFO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWalletInfoWT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletService walletService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    public void successGetWalletInfo() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        UUID walletId = walletService.createWallet(createWalletRequestDto);

//        Act & Assert
        mockMvc.perform(get(PATH_GET_WALLET_INFO.replace("{id}", walletId.toString()))
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isOk());
    }

    @Test
    public void failOnUnauthorizedRequest() throws Exception {
//        Arrange
        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        UUID walletId = walletService.createWallet(createWalletRequestDto);

//        Act & Assert
        mockMvc.perform(get(PATH_GET_WALLET_INFO.replace("{id}", walletId.toString()))
                        .header(xTraceIdKey, xTraceIdValue))
                .andExpect(status().isUnauthorized());
    }
}