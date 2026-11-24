package aq.project.test.web;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.WebhookServiceEntities;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.utils.security.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.UUID;

import static aq.project.controller.WebhookRestControllerApi.PATH_CONFIRM_OPERATION_WEBHOOK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmOperationWT {

    @Value("${app.security.access-token-key}")
    private String accessToken;

    @Value("${app.security.access-token-hash-alg}")
    private String accessTokenHashAlg;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    public void successConfirmOperation() throws Exception {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        String json = objectMapper.writeValueAsString(updateStatusOperationRequestDto);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String xAccessTokenHashKey = "x-access-token-hash";
        String xAccessTokenHashValue = SecurityUtil.getHash(accessToken, accessTokenHashAlg);

        String xAccessTokenHashAlgKey = "x-access-token-hash-alg";
        String xAccessTokenHashAlgValue = accessTokenHashAlg;

//        Act & Assert
        mockMvc.perform(post(PATH_CONFIRM_OPERATION_WEBHOOK)
                        .header(xTraceIdKey, xTraceIdValue)
                        .header(xAccessTokenHashKey, xAccessTokenHashValue)
                        .header(xAccessTokenHashAlgKey, xAccessTokenHashAlgValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void failureOnInvalidAccessTokenValue() throws Exception {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        String json = objectMapper.writeValueAsString(updateStatusOperationRequestDto);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String xAccessTokenHashKey = "x-access-token-hash";
        String xAccessTokenHashValue = SecurityUtil.getHash(UUID.randomUUID().toString(), accessTokenHashAlg);

        String xAccessTokenHashAlgKey = "x-access-token-hash-alg";
        String xAccessTokenHashAlgValue = accessTokenHashAlg;

//        Act & Assert
        mockMvc.perform(post(PATH_CONFIRM_OPERATION_WEBHOOK)
                        .header(xTraceIdKey, xTraceIdValue)
                        .header(xAccessTokenHashKey, xAccessTokenHashValue)
                        .header(xAccessTokenHashAlgKey, xAccessTokenHashAlgValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void failureOnInvalidAccessTokenHashAlg() throws Exception {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        String json = objectMapper.writeValueAsString(updateStatusOperationRequestDto);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String xAccessTokenHashKey = "x-access-token-hash";
        String xAccessTokenHashValue = SecurityUtil.getHash(UUID.randomUUID().toString(), "SHA-1");

        String xAccessTokenHashAlgKey = "x-access-token-hash-alg";
        String xAccessTokenHashAlgValue = "SHA-1";

//        Act & Assert
        mockMvc.perform(post(PATH_CONFIRM_OPERATION_WEBHOOK)
                        .header(xTraceIdKey, xTraceIdValue)
                        .header(xAccessTokenHashKey, xAccessTokenHashValue)
                        .header(xAccessTokenHashAlgKey, xAccessTokenHashAlgValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void failureOnInvalidRequiredHeadersNotFoundAlg() throws Exception {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        String json = objectMapper.writeValueAsString(updateStatusOperationRequestDto);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String xAccessTokenHashKey = "x-access-token-hash";
        String xAccessTokenHashValue = SecurityUtil.getHash(accessToken, accessTokenHashAlg);

//        Act & Assert
        mockMvc.perform(post(PATH_CONFIRM_OPERATION_WEBHOOK)
                        .header(xTraceIdKey, xTraceIdValue)
                        .header(xAccessTokenHashKey, xAccessTokenHashValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void failureOnInvalidRequestPath() throws Exception {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        String json = objectMapper.writeValueAsString(updateStatusOperationRequestDto);

        String xTraceIdKey = "x-trace-id";
        String xTraceIdValue = UUID.randomUUID().toString();

        String xAccessTokenHashKey = "x-access-token-hash";
        String xAccessTokenHashValue = SecurityUtil.getHash(accessToken, accessTokenHashAlg);

        String xAccessTokenHashAlgKey = "x-access-token-hash-alg";
        String xAccessTokenHashAlgValue = accessTokenHashAlg;

//        Act & Assert
        mockMvc.perform(post("/api/v1/webhook/wrong-path")
                        .header(xTraceIdKey, xTraceIdValue)
                        .header(xAccessTokenHashKey, xAccessTokenHashValue)
                        .header(xAccessTokenHashAlgKey, xAccessTokenHashAlgValue)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}