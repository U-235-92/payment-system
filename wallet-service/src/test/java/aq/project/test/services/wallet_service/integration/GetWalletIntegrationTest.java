package aq.project.test.services.wallet_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.wallet.WalletRepository;
import aq.project.services.wallet.WalletService;
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

import java.util.UUID;

import static aq.project._utils.WalletEntities.getValidWallet;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWalletIntegrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;
    @Container
    private static final KeycloakContainer KEYCLOAK_CONTAINER = Containers.KEYCLOAK_CONTAINER;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
        ContainerPropertiesConfigurer.registerApplicationContextKeycloakContainerProperties(registry, KEYCLOAK_CONTAINER);
    }

    @AfterEach
    void tearDownRepository() {
        walletRepository.deleteAll();
    }

    @Test
    public void successGetWallet() {
        // Arrange & Act
        Wallet wallet = getValidWallet();
        UUID walletId = walletRepository.save(wallet).getId();

        Wallet found = walletService.getWallet(walletId);

        // Assert
        Assertions.assertNotNull(found);
        Assertions.assertEquals(walletId, found.getId());
    }

    @Test
    public void failGetWalletOnNotFound() {
        // Arrange & Act
        UUID nonExistentId = UUID.randomUUID();

        // Assert
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> walletService.getWallet(nonExistentId));
    }

    @Test
    public void failGetWalletOnNullId() {
        // Arrange & Act
        UUID nullId = null;

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.getWallet(nullId));
    }
}