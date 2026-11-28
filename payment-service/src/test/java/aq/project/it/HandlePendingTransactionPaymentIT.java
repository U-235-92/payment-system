package aq.project.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.entities.PaymentServiceEntities;
import aq.project.dto.ErrorDto;
import aq.project.dto.PaymentStatus;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.entities.Payment;
import aq.project.entities.PaymentMethod;
import aq.project.entities.PaymentMethodDefinition;
import aq.project.entities.PaymentMethodRequiredField;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.PaymentMethodDefinitionRepository;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentMethodRequiredFieldRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.services.TransactionService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(value = @ConfigureWireMock(name = "fake-payment-provider-service", port = 18085))
public class HandlePendingTransactionPaymentIT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @InjectWireMock("fake-payment-provider-service")
    private WireMockServer fakePaymentProviderServiceMock;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private PaymentMethodDefinitionRepository paymentMethodDefinitionRepository;

    @Autowired
    private PaymentMethodRequiredFieldRepository paymentMethodRequiredFieldRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void setUpConfigurationProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);

        registry.add("app.service.fake-payment-provider-service.uri", () -> "http://localhost:18085");
    }

    @Test
    public void successHandlePendingPayoutPayment() {
//        Arrange
        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(PaymentServiceEntities.getPaymentMethodDefinition());

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(PaymentServiceEntities.getPaymentMethodRequiredField());

        PaymentMethod newPaymentMethod = PaymentServiceEntities
                .getPaymentMethod(savedPaymentMethodDefinition, savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        Payment newPayment = PaymentServiceEntities.getPayment(savedPaymentMethod);

        newPayment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(newPayment);

        PayoutInfoResponseDto payoutInfoResponseDto = PaymentServiceEntities.getPayoutInfoResponseDto();

        String transactionEndpoint = "/api/v1/transactions/create";

        fakePaymentProviderServiceMock.stubFor(WireMock.post(transactionEndpoint)
                .willReturn(ResponseDefinitionBuilder.okForJson(payoutInfoResponseDto)));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> transactionService.handlePendingTransactionPayment(newPayment));

        PaymentStatus paymentStatus = paymentRepository.findById(savedPayment.getId()).get().getStatus();

        Assertions.assertEquals(PaymentStatus.PROCESSING, paymentStatus);
    }

    @Test
    public void failureOnFakeProvider4xxResponse() {
//        Arrange
        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(PaymentServiceEntities.getPaymentMethodDefinition());

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(PaymentServiceEntities.getPaymentMethodRequiredField());

        PaymentMethod newPaymentMethod = PaymentServiceEntities
                .getPaymentMethod(savedPaymentMethodDefinition, savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        Payment newPayment = PaymentServiceEntities.getPayment(savedPaymentMethod);

        newPayment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(newPayment);

        String transactionEndpoint = "/api/v1/transactions/create";

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        fakePaymentProviderServiceMock.stubFor(WireMock.post(transactionEndpoint)
                .willReturn(WireMock.badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        Assertions.assertThrows(HttpClientErrorException.class,
                () -> transactionService.handlePendingTransactionPayment(savedPayment));
    }

    @Test
    public void failureOnFakeProvider5xxResponse() {
//        Arrange
        PaymentMethodDefinition savedPaymentMethodDefinition = paymentMethodDefinitionRepository
                .save(PaymentServiceEntities.getPaymentMethodDefinition());

        PaymentMethodRequiredField savedPaymentMethodRequiredField = paymentMethodRequiredFieldRepository
                .save(PaymentServiceEntities.getPaymentMethodRequiredField());

        PaymentMethod newPaymentMethod = PaymentServiceEntities
                .getPaymentMethod(savedPaymentMethodDefinition, savedPaymentMethodRequiredField);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(newPaymentMethod);

        Payment newPayment = PaymentServiceEntities.getPayment(savedPaymentMethod);

        newPayment.setStatus(PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(newPayment);

        String transactionEndpoint = "/api/v1/transactions/create";

        String errorJson = objectMapper.writeValueAsString(ErrorDto.builder().message("Error").build());

        fakePaymentProviderServiceMock.stubFor(WireMock.post(transactionEndpoint)
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorJson)
                ));

//        Act & Assert
        Assertions.assertThrows(FallbackOperationException.class,
                () -> transactionService.handlePendingTransactionPayment(savedPayment));
    }
}