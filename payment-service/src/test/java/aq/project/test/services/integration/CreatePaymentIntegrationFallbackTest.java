package aq.project.test.services.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PaymentProviderServiceCreatePaymentResponseDto;
import aq.project.dto.PaymentServiceCreatePaymentRequestDto;
import aq.project.entities.PaymentMethod;
import aq.project.entities.PaymentMethodDefinition;
import aq.project.entities.PaymentMethodRequiredField;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.PaymentMethodDefinitionRepository;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentMethodRequiredFieldRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.services.PaymentService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.stream.Stream;

import static aq.project._utils.entities.PaymentServiceEntities.*;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(name = "payment-provider-mock-service", port = 8558))
public class CreatePaymentIntegrationFallbackTest {

    private static final String[] LOWERCASE_ALPHABET = Stream.iterate(97, code -> code + 1)
            .limit(26)
            .map(Character::toString)
            .toArray(String[]::new);

    private static final String[] UPPERCASE_ALPHABET = Stream.iterate(65, code -> code + 1)
            .limit(26)
            .map(Character::toString)
            .toArray(String[]::new);

    @Value("${service.payment-provider-service.endpoints.create-payment}")
    private String createPaymentOnPaymentProviderServiceEndpoint;
    @Value("${service.payment-provider-service.endpoints.fail-payment}")
    private String failPaymentOnPaymentProviderServiceEndpoint;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @InjectWireMock("payment-provider-mock-service")
    private WireMockServer paymentProviderMockService;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentMethodDefinitionRepository paymentMethodDefinitionRepository;
    @Autowired
    private PaymentMethodRequiredFieldRepository paymentMethodRequiredFieldRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentService paymentService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
        registry.add("service.payment-provider-service.uri", () -> "http://localhost:8558");
    }

    @AfterEach
    void cleanRepositories() {
        paymentRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        paymentMethodDefinitionRepository.deleteAll();
        paymentMethodRequiredFieldRepository.deleteAll();
    }

    @Test
    public void failCreatePaymentOnFallbackException() {
//        Arrange
        final int RATE_LIMIT = 10;

        for(int i = 0; i < RATE_LIMIT; i++) {
            PaymentMethodDefinition newPaymentMethodDefinition = getValidPaymentMethodDefinition();
            newPaymentMethodDefinition.setId(null);

            PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                    .save(newPaymentMethodDefinition);

            PaymentMethodRequiredField newPaymentMethodRequiredField = getValidPaymentMethodRequiredField();
            newPaymentMethodRequiredField.setId(null);
            newPaymentMethodRequiredField.setLanguage(genRandomString(LOWERCASE_ALPHABET, 2));
            newPaymentMethodRequiredField.setCountryAlpha3Code(genRandomString(UPPERCASE_ALPHABET, 3));
            newPaymentMethodRequiredField.setCurrencyCode(genRandomString(UPPERCASE_ALPHABET, 3));
            newPaymentMethodRequiredField.setName(genRandomString(UPPERCASE_ALPHABET, 10));

            PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                    .save(newPaymentMethodRequiredField);

            PaymentMethod newPaymentMethod = getValidPaymentMethod();
            newPaymentMethod.setId(null);
            newPaymentMethod.setDefinition(savedPaymentMethodDefinition);
            newPaymentMethod.setRequiredFields(savedPaymentMethodRequiredField);

            paymentMethodRepository.save(newPaymentMethod);
        }

        Iterable<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();

        for(int i = 0; i < RATE_LIMIT && paymentMethods.iterator().hasNext(); i++) {
            PaymentMethod paymentMethod = paymentMethods.iterator().next();

            UUID transactionId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();
            Long paymentMethodId = paymentMethod.getId();

            PaymentServiceCreatePaymentRequestDto paymentServiceCreatePaymentRequestDto = getValidPaymentServiceCreatePaymentRequestDto();
            paymentServiceCreatePaymentRequestDto.setTransactionId(transactionId);
            paymentServiceCreatePaymentRequestDto.setPaymentMethodId(paymentMethodId);

            PaymentProviderServiceCreatePaymentResponseDto paymentProviderServiceCreatePaymentResponseDto = getValidPaymentProviderServiceCreatePaymentResponseDto();
            paymentProviderServiceCreatePaymentResponseDto.setPaymentId(paymentId);
            paymentProviderServiceCreatePaymentResponseDto.setTransactionId(transactionId);

            String json = objectMapper.writeValueAsString(paymentProviderServiceCreatePaymentResponseDto);

            paymentProviderMockService.stubFor(WireMock.post(createPaymentOnPaymentProviderServiceEndpoint)
                    .willReturn(WireMock.okJson(json)));

//        Act & Assert
            if(i < RATE_LIMIT - 1)
                new Thread(() -> paymentService.createPayment(paymentServiceCreatePaymentRequestDto)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> paymentService.createPayment(paymentServiceCreatePaymentRequestDto));
        }
    }

    private String genRandomString(String[] alphabet, int length) {
        StringBuilder gen = new StringBuilder();
        for(int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * (alphabet.length));
            gen.append(alphabet[idx]);
        }
        return gen.toString();
    }
}
