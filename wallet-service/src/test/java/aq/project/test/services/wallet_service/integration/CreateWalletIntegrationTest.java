package aq.project.test.services.wallet_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.entities.wallet.Wallet;
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

import static aq.project._utils.WalletEntities.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateWalletIntegrationTest {

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
    public void successCreateWallet() {
        // Arrange & Act
        Wallet wallet = getValidWallet();
        UUID walletId = walletService.createWallet(wallet);

        // Assert
        Assertions.assertNotNull(walletId);
        Assertions.assertTrue(walletRepository.findById(walletId).isPresent());
    }

    @Test
    public void failCreateWalletOnInvalidWalletDetails() {
        // Arrange & Act
        Wallet wallet = getValidWallet();
        wallet.setWalletDetails(getInvalidWalletDetails()); // нарушает @NotBlank

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.createWallet(wallet));
    }

    @Test
    public void failCreateWalletOnInvalidCreditCard() {
        // Arrange & Act
        Wallet wallet = getValidWallet();
        wallet.setCreditCard(getInvalidCreditCard()); // нарушает регулярку

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.createWallet(wallet));
    }

    @Test
    public void failCreateWalletOnMissingCreditCard() {
        // Arrange & Act
        Wallet wallet = getValidWallet();
        wallet.setCreditCard(null); // нарушает @NotNull в аспекте

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.createWallet(wallet));
    }

    @Test
    public void failCreateWalletOnNullWallet() {
        // Arrange & Act
        Wallet wallet = null;

        // Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.createWallet(wallet));
    }
}