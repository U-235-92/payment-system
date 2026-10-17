package aq.project.services.payment_service.integrtion;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.PaymentProviderServiceCreatePaymentRequestDto;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.PaymentRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.services.PaymentService;
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

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePaymentFallbackIntegrationTest {

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
    public void failCreatePaymentOnExceededRateLimit() {
//        Arrange
        Merchant merchant = getValidMerchant();

        Transaction transaction = getValidTransaction();
        transaction.setMerchant(merchant);

        merchantRepository.save(merchant);

        Transaction savedTransaction = transactionRepository.save(transaction);

        PaymentProviderServiceCreatePaymentRequestDto paymentProviderServiceCreatePaymentRequestDto = getValidPaymentProviderServiceCreatePaymentRequestDto();
        paymentProviderServiceCreatePaymentRequestDto.setTransactionId(savedTransaction.getId());

        final int RATE_LIMIT = 50;

//        Act & Assert
        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1)
                new Thread(() -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto)).start();
            else
                Assertions.assertThrows(FallbackOperationException.class,
                        () -> paymentService.createPayment(paymentProviderServiceCreatePaymentRequestDto));
        }
    }
}
