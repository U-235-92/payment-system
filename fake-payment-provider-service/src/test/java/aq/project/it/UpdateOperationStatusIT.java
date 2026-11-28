package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.WebhookServiceEntities;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.WebhookService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
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

import static com.github.tomakehurst.wiremock.client.WireMock.post;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(value = @ConfigureWireMock(name = "webhook-service", port = 18085))
public class UpdateOperationStatusIT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @InjectWireMock("webhook-service")
    private WireMockServer webhookServiceMock;

    @Autowired
    private WebhookService webhookService;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);

        registry.add("app.service.webhook-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successUpdateOperationStatus() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        String confirmOperationEndpoint = "/api/v1/webhook/payment-provider";

        webhookServiceMock.stubFor(post(confirmOperationEndpoint)
                .willReturn(WireMock.ok()));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> webhookService.updateOperationStatus(updateStatusOperationRequestDto));
    }

    @Test
    public void failureOnInvalidDto() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();
        updateStatusOperationRequestDto.setTransactionId(null);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> webhookService.updateOperationStatus(updateStatusOperationRequestDto));
    }

    @Test
    public void failureOnWebhookService4xxResponse() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        String confirmOperationEndpoint = "/api/v1/webhook/payment-provider";

        webhookServiceMock.stubFor(post(confirmOperationEndpoint)
                .willReturn(WireMock.badRequest()));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> webhookService.updateOperationStatus(updateStatusOperationRequestDto));
    }

    @Test
    public void failureOnWebhookService5xxResponse() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        String confirmOperationEndpoint = "/api/v1/webhook/payment-provider";

        webhookServiceMock.stubFor(post(confirmOperationEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
        () -> webhookService.updateOperationStatus(updateStatusOperationRequestDto));
    }

    @Test
    public void failureOnRetryExceeded() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        String confirmOperationEndpoint = "/api/v1/webhook/payment-provider";

        webhookServiceMock.stubFor(post(confirmOperationEndpoint)
                .willReturn(WireMock.serverError()));

//        Act & Assert
        FallbackAssert.assertThrowsRetryLimit(FallbackOperationException.class,
                () -> webhookService.updateOperationStatus(updateStatusOperationRequestDto));
    }
}