package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.PaymentServiceEntities;
import aq.project.dto.CreatePaymentRequestDto;
import aq.project.entities.PaymentMethod;
import aq.project.entities.PaymentMethodDefinition;
import aq.project.entities.PaymentMethodRequiredField;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.PaymentMethodDefinitionRepository;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentMethodRequiredFieldRepository;
import aq.project.services.PaymentService;
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

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePaymentIT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private PaymentMethodDefinitionRepository paymentMethodDefinitionRepository;

    @Autowired
    private PaymentMethodRequiredFieldRepository paymentMethodRequiredFieldRepository;

    @Autowired
    private PaymentService paymentService;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    public void successCreatePayment() {
//        Arrange
        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(PaymentServiceEntities.getPaymentMethodDefinition());

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(PaymentServiceEntities.getPaymentMethodRequiredField());

        PaymentMethod newPaymentMethod = PaymentServiceEntities
                .getPaymentMethod(savedPaymentMethodDefinition, savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        CreatePaymentRequestDto createPaymentRequestDto = PaymentServiceEntities.getCreatePaymentRequestDto();

        createPaymentRequestDto.setPaymentMethodId(savedPaymentMethod.getId());
        createPaymentRequestDto.setCurrencyCode(savedPaymentMethod.getRequiredFields().getCurrencyCode());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.createPayment(createPaymentRequestDto));
    }

    @Test
    public void failureOnPaymentMethodNotFound() {
//        Arrange
        CreatePaymentRequestDto createPaymentRequestDto = PaymentServiceEntities.getCreatePaymentRequestDto();

        createPaymentRequestDto.setPaymentMethodId(85L);

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> paymentService.createPayment(createPaymentRequestDto));
    }

    @Test
    public void failureOnInvalidRequestDto() {
//        Arrange
        CreatePaymentRequestDto createPaymentRequestDto = PaymentServiceEntities.getCreatePaymentRequestDto();

        createPaymentRequestDto.setTransactionId(null);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentService.createPayment(createPaymentRequestDto));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(PaymentServiceEntities.getPaymentMethodDefinition());

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(PaymentServiceEntities.getPaymentMethodRequiredField());

        PaymentMethod newPaymentMethod = PaymentServiceEntities
                .getPaymentMethod(savedPaymentMethodDefinition, savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        CreatePaymentRequestDto createPaymentRequestDto = PaymentServiceEntities.getCreatePaymentRequestDto();

        createPaymentRequestDto.setPaymentMethodId(savedPaymentMethod.getId());
        createPaymentRequestDto.setCurrencyCode(savedPaymentMethod.getRequiredFields().getCurrencyCode());

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> paymentService.createPayment(createPaymentRequestDto));
    }
}