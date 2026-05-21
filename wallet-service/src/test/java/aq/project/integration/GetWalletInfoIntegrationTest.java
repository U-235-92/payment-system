package aq.project.integration;

import aq.project.configs.ContainersConfigurer;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.repositories.WalletRepository;
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

import java.util.UUID;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWalletInfoIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @DynamicPropertySource
    static void configDynamicPropertySource(DynamicPropertyRegistry registry) {
        ContainersConfigurer.configurePostgreSqlProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    public void successfulGetWalletInfoTest() throws NoSuchWalletException {
        String walletId = walletRepository.save(TestWalletMocks.getValidWalletMock()).getId();
        Assertions.assertNotNull(walletService.getWalletInfo(walletId));
    }

    @Test
    public void failGetWalletInfoWithUnknownWalletIdTest() {
        Assertions.assertThrows(NoSuchWalletException.class, () -> walletService.getWalletInfo(UUID.randomUUID().toString()));
    }

    @Test
    public void failGetWalletInfoWithWrongWalletIdTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> walletService.getWalletInfo("abc"));
    }

    @Test
    public void failGetWalletInfoWithNullWalletIdTest() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> walletService.getWalletInfo(null));
    }
}
