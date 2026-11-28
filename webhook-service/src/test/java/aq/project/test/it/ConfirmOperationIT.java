package aq.project.test.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.WebhookServiceEntities;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.exceptions.UnknownOperationTypeException;
import aq.project.repositories.PaymentProviderCallbackRepository;
import aq.project.repositories.UnknownCallbackRepository;
import aq.project.services.WebhookService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
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

import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfirmOperationIT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private PaymentProviderCallbackRepository paymentProviderCallbackRepository;

    @Autowired
    private UnknownCallbackRepository unknownCallbackRepository;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void cleanUpRepositories() {
        paymentProviderCallbackRepository.deleteAll();
        unknownCallbackRepository.deleteAll();
    }

    @Test
    public void successConfirmOperation() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        UUID transactionId = updateStatusOperationRequestDto.getTransactionId();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> webhookService.confirmOperation(updateStatusOperationRequestDto));

        Assertions.assertTrue(() -> paymentProviderCallbackRepository.findByTransactionId(transactionId).isPresent());
    }

    @Test
    public void successConfirmOperationOnDuplicateRequestByOperationId() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationFirstRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        UpdateStatusOperationRequestDto updateStatusOperationSecondRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> webhookService.confirmOperation(updateStatusOperationFirstRequestDto));

        Assertions.assertDoesNotThrow(() -> webhookService.confirmOperation(updateStatusOperationSecondRequestDto));
    }

    @Test
    public void successConfirmOperationOnDuplicateRequestByTransactionId() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationFirstRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        UpdateStatusOperationRequestDto updateStatusOperationSecondRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        updateStatusOperationSecondRequestDto.setId(UUID.randomUUID());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> webhookService.confirmOperation(updateStatusOperationFirstRequestDto));

        Assertions.assertDoesNotThrow(() -> webhookService.confirmOperation(updateStatusOperationSecondRequestDto));
    }

    @Test
    public void failureOnUnknownOperationTypeRequest() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        updateStatusOperationRequestDto.setOperationType("UNKNOWN_OPERATION");

        UUID operationId = updateStatusOperationRequestDto.getId();

//        Act & Assert
        Assertions.assertThrows(UnknownOperationTypeException.class,
                () -> webhookService.confirmOperation(updateStatusOperationRequestDto));

        Assertions.assertTrue(() -> unknownCallbackRepository.findById(operationId).isPresent());
    }

    @Test
    public void failureOnInvalidDtoRequest() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

        updateStatusOperationRequestDto.setId(null);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> webhookService.confirmOperation(updateStatusOperationRequestDto));
    }

    @Test
    public void failureOnRateLimiterExceeded() {
//        Arrange
        UpdateStatusOperationRequestDto updateStatusOperationRequestDto = WebhookServiceEntities
                .getUpdateStatusOperationRequestDto();

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> webhookService.confirmOperation(updateStatusOperationRequestDto));
    }
}