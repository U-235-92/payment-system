package aq.project.integration;

import aq.project.configs.ContainersConfigurer;
import aq.project.exceptions.TransactionException;
import aq.project.mocks.TestTransactionMocks;
import aq.project.repositories.TransactionRepository;
import aq.project.services.TransactionService;
import aq.project.utils.Containers;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;
import java.util.UUID;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTransactionStatusIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @DynamicPropertySource
    static void configDynamicPropertySource(DynamicPropertyRegistry registry) {
        ContainersConfigurer.configurePostgreSqlProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    public void successGetTransactionStatusUnitTest() {
        String id = UUID.randomUUID().toString();
        Mockito.when(transactionRepository.findById(id)).thenReturn(Optional.of(TestTransactionMocks.getValidTransaction()));
        Assertions.assertDoesNotThrow(() -> transactionService.getTransactionStatus(id));
    }

    @Test
    public void failGetTransactionStatusWithUnknownTransactionIdUnitTest() {
        String knownOutboxEventId = UUID.randomUUID().toString();
        String unknownOutboxEventId = UUID.randomUUID().toString();
        Mockito.when(transactionRepository.findById(knownOutboxEventId)).thenReturn(Optional.of(TestTransactionMocks.getValidTransaction()));
        Assertions.assertThrows(TransactionException.class, () -> transactionService.getTransactionStatus(unknownOutboxEventId));
    }

    @Test
    public void failGetTransactionStatusWithInvalidTransactionIdUnitTest() {
        String id = "invalid-id";
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService.getTransactionStatus(id));
    }

    @Test
    public void failGetTransactionStatusWithNullTransactionIdUnitTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> transactionService.getTransactionStatus(null));
    }
}
