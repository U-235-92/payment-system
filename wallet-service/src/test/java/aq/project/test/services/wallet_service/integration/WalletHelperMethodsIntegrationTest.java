package aq.project.test.services.wallet_service.integration;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project.dto.WalletStatus;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.repositories.wallet.WalletRepository;
import aq.project.services.wallet.WalletService;
import dasniko.testcontainers.keycloak.KeycloakContainer;
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

import java.math.BigDecimal;

import static aq.project._utils.WalletEntities.*;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WalletHelperMethodsIntegrationTest {

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
    public void successIsWalletBlocked() {
        // Arrange & Act
        Wallet wallet = getValidWallet();
        wallet.getWalletDetails().setWalletStatus(WalletStatus.BLOCKED);

        boolean blocked = walletService.isWalletBlocked(wallet);

        // Assert
        Assertions.assertTrue(blocked);
    }

    @Test
    public void successIsWalletNotBlocked() {
        // Arrange & Act
        Wallet wallet = getValidWallet();

        boolean blocked = walletService.isWalletBlocked(wallet);

        // Assert
        Assertions.assertFalse(blocked);
    }

    @Test
    public void successIsWalletCreditCardExpired() {
        // Arrange & Act
        CreditCard expiredCard = getInvalidCreditCardExpired();

        boolean expired = walletService.isWalletCreditCardExpired(expiredCard);

        // Assert
        Assertions.assertTrue(expired);
    }

    @Test
    public void successIsWalletCreditCardNotExpired() {
        // Arrange & Act
        CreditCard validCard = getValidCreditCard();

        boolean expired = walletService.isWalletCreditCardExpired(validCard);

        // Assert
        Assertions.assertFalse(expired);
    }

    @Test
    public void successIsWalletCreditCardBalanceLessThan() {
        // Arrange & Act
        CreditCard card = getValidCreditCard();
        card.setBalance(BigDecimal.valueOf(100.00));
        BigDecimal amount = BigDecimal.valueOf(150.00);

        boolean less = walletService.isWalletCreditCardBalanceLessThan(card, amount);

        // Assert
        Assertions.assertTrue(less);
    }

    @Test
    public void successIsWalletCreditCardBalanceNotLessThan() {
        // Arrange & Act
        CreditCard card = getValidCreditCard();
        card.setBalance(BigDecimal.valueOf(200.00));
        BigDecimal amount = BigDecimal.valueOf(150.00);

        boolean less = walletService.isWalletCreditCardBalanceLessThan(card, amount);

        // Assert
        Assertions.assertFalse(less);
    }
}