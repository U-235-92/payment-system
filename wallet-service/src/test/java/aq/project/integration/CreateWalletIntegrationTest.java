package aq.project.integration;

import aq.project.configs.ContainersConfigurer;
import aq.project.services.WalletService;
import aq.project.mocks.TestWalletMocks;
import aq.project.utils.Containers;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateWalletIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @DynamicPropertySource
    static void configDynamicPropertySource(DynamicPropertyRegistry registry) {
        ContainersConfigurer.configurePostgreSqlProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    public void successfulWalletCreation() {
        Assertions.assertDoesNotThrow(() -> walletService.createWallet(TestWalletMocks.getValidWalletMock()));
    }

    @Test
    public void failedWalletCreationWithInvalidWalletData() {
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.createWallet(TestWalletMocks.getInvalidWalletMock()));
    }

    @Test
    public void failedWalletCreationWithNullWallet() {
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.createWallet(null));
    }

    @Test
    public void failedWalletCreationWithWrongUuidData() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(TestWalletMocks.getInvalidWalletMockWithWrongUuidData()));
    }
}
