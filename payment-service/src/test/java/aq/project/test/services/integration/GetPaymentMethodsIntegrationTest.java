package aq.project.test.services.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.PaymentMethod;
import aq.project.entities.PaymentMethodDefinition;
import aq.project.entities.PaymentMethodRequiredField;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.PaymentMethodDefinitionRepository;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentMethodRequiredFieldRepository;
import aq.project.services.PaymentService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
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

import static aq.project._utils.entities.PaymentServiceEntities.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetPaymentMethodsIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentMethodDefinitionRepository paymentMethodDefinitionRepository;
    @Autowired
    private PaymentMethodRequiredFieldRepository paymentMethodRequiredFieldRepository;

    @Autowired
    private PaymentService paymentService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void cleanRepositories() {
        paymentMethodRepository.deleteAll();
        paymentMethodDefinitionRepository.deleteAll();
        paymentMethodRequiredFieldRepository.deleteAll();
    }

    @Test
    public void successGetPaymentMethods() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        PaymentMethodDefinition newPaymentMethodDefinition = getValidPaymentMethodDefinition();
        newPaymentMethodDefinition.setId(null);

        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(newPaymentMethodDefinition);

        PaymentMethodRequiredField newPaymentMethodRequiredField = getValidPaymentMethodRequiredField();
        newPaymentMethodRequiredField.setId(null);
        newPaymentMethodRequiredField.setCurrencyCode(currencyCode);
        newPaymentMethodRequiredField.setCountryAlpha3Code(countryCode);

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(newPaymentMethodRequiredField);

        PaymentMethod newPaymentMethod = getValidPaymentMethod();
        newPaymentMethod.setId(null);
        newPaymentMethod.setDefinition(savedPaymentMethodDefinition);
        newPaymentMethod.setRequiredFields(savedPaymentMethodRequiredField);

        paymentMethodRepository.save(newPaymentMethod);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.getPaymentMethods(currencyCode, countryCode));
        Assertions.assertFalse(paymentService.getPaymentMethods(currencyCode, countryCode).isEmpty());
    }

    @Test
    public void successGetPaymentMethodsOnEmptyPaymentMethodList() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.getPaymentMethods(currencyCode, countryCode));
    }

    @Test
    public void failGetPaymentMethodsOnInvalidArguments() {
//        Arrange
        String currencyCode = "";
        String countryCode = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentService.getPaymentMethods(currencyCode, countryCode));
    }

    @Test
    public void failGetPaymentMethodsOnFallbackException() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        final int RATE_LIMIT = 50;

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1)
                new Thread(() -> paymentService.getPaymentMethods(currencyCode, countryCode)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> paymentService.getPaymentMethods(currencyCode, countryCode));
        }
    }
}
