package aq.project.services.payment_service.integrtion;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PaymentProviderServiceFailPaymentRequestDto;
import aq.project.dto.PaymentStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Payment;
import aq.project.entities.Transaction;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.services.PaymentService;
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

import static aq.project._utils.entities.PaymentServiceEntities.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FailPaymentIntegrationTest {

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

    @AfterEach
    public void tearDownRepositories() {
        paymentRepository.deleteAll();
        transactionRepository.deleteAll();
        merchantRepository.deleteAll();
    }

    @Test
    public void successFailPayment() {
//        Arrange
        UUID transactionId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Merchant merchant = getValidMerchant();

        Transaction transaction = getValidTransaction();
        transaction.setId(transactionId);
        transaction.setMerchant(merchant);

        Payment payment = getValidPayment();
        payment.setId(paymentId);
        payment.setTransaction(transaction);
        payment.setStatus(PaymentStatus.PENDING);

        PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto = getValidPaymentProviderServiceFailPaymentRequestDto();
        paymentProviderServiceFailPaymentRequestDto.setTransactionId(transactionId);
        paymentProviderServiceFailPaymentRequestDto.setPaymentId(paymentId);

        merchantRepository.save(merchant);
        transactionRepository.save(transaction);
        paymentRepository.save(payment);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.failPayment(paymentProviderServiceFailPaymentRequestDto));
        Assertions.assertEquals(PaymentStatus.FAILED, paymentRepository.findById(paymentId).get().getStatus());
    }

    @Test
    public void failCreatePaymentOnInvalidDto() {
//        Arrange
        PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto = getInvalidPaymentProviderServiceFailPaymentRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentService.failPayment(paymentProviderServiceFailPaymentRequestDto));
    }

    @Test
    public void failCreatePaymentOnNullDto() {
//        Arrange
        PaymentProviderServiceFailPaymentRequestDto paymentProviderServiceFailPaymentRequestDto = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> paymentService.failPayment(paymentProviderServiceFailPaymentRequestDto));
    }
}
