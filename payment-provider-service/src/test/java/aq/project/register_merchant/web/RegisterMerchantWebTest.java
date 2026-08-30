package aq.project.register_merchant.web;

import aq.project.dto.MerchantRegistrationRequestDto;
import aq.project.entities.Merchant;
import aq.project.repositories.MerchantRepository;
import aq.project._utils.Containers;
import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project.utils.mappers.MerchantMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
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

import java.util.UUID;

import static aq.project.controller.AdminRestControllerApi.PATH_REGISTER_MERCHANT;
import static aq.project._utils.Entities.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterMerchantWebTest {

    private final MerchantMapper merchantMapper = MerchantMapper.INSTANCE;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void tearDownRepositories() {
        merchantRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "ADMIN")
    public void successRegisterMerchantWebTest() throws Exception {
//        Arrange
        MerchantRegistrationRequestDto dto = getValidUserMerchantRegistrationRequestDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_REGISTER_MERCHANT)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "ADMIN")
    public void failRegisterMerchantOnInvalidMerchantRegistrationRequestDtoWebTest() throws Exception {
//        Arrange
        MerchantRegistrationRequestDto dto = getInvalidUserMerchantRegistrationRequestDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_REGISTER_MERCHANT)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "ADMIN")
    public void failRegisterMerchantOnDuplicateMerchantRegistrationRequestDtoWebTest() throws Exception {
//        Arrange
        MerchantRegistrationRequestDto dto = getValidUserMerchantRegistrationRequestDto();
        Merchant merchant = merchantMapper.toMerchant(dto);
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

        merchantRepository.save(merchant);

//        Act & Assert
        mockMvc.perform(post(PATH_REGISTER_MERCHANT)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "test-merchant-service", password = "secret", roles = "USER")
    public void failRegisterMerchantOnUnexpectedRoleWebTest() throws Exception {
//        Arrange
        MerchantRegistrationRequestDto dto = getValidUserMerchantRegistrationRequestDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_REGISTER_MERCHANT)
                        .header(HttpHeaders.AUTHORIZATION, getBase64BasicAuthorizationValue())
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void failRegisterMerchantOnUnauthorizedRequestWebTest() throws Exception {
//        Arrange
        MerchantRegistrationRequestDto dto = getValidUserMerchantRegistrationRequestDto();
        String xTraceKey = "x-trace-id";
        String xTraceValue = UUID.randomUUID().toString();

//        Act & Assert
        mockMvc.perform(post(PATH_REGISTER_MERCHANT)
                        .header(xTraceKey, xTraceValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
