package aq.project.services.payment_service.integrtion;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.services.PaymentService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static aq.project._utils.entities.PaymentServiceEntities.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePaymentIntegrationTest {

    private static final String TEST_MERCHANT_ID = "test-merchant-id";

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private PaymentService paymentService;

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @BeforeEach
    void setUpSecurity() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getPrincipal()).thenReturn(TEST_MERCHANT_ID);
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDownSecurity() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDownRepositories() {
        paymentRepository.deleteAll();
        transactionRepository.deleteAll();
        merchantRepository.deleteAll();
    }

    @Test
    public void successCreatePayment() {
//        Arrange
        Merchant merchant = getValidMerchant();
        merchant.setId(TEST_MERCHANT_ID);

        Transaction transaction = getValidTransaction();
        transaction.setMerchant(merchant);

        merchantRepository.save(merchant);

        Transaction savedTransaction = transactionRepository.save(transaction);

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(savedTransaction.getId());
        paymentProviderServiceCreatePaymentRequestDto.setMerchantId(TEST_MERCHANT_ID);

//        Act & Asset
        Assertions.assertDoesNotThrow(() -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto));
    }

    @Test
    public void failCreatePaymentOnInvalidDto() {
//        Arrange
        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getInvalidPaymentProviderServiceCreatePaymentRequestDto();

//        Act & Asset
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto));
    }

    @Test
    public void failCreatePaymentOnNullDto() {
//        Arrange
        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = null;

//        Act & Asset
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto));
    }
}
