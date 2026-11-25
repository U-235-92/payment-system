package aq.project.test.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.WalletServiceEntities;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.WalletOperationRepository;
import aq.project.services.WalletService;
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

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetWalletInfoIT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private WalletOperationRepository walletOperationRepository;

    @Autowired
    private WalletService walletService;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void cleanUpRepositories() {
        walletOperationRepository.deleteAll();
    }

    @Test
    public void successGetWalletInfo() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        UUID walletId = walletService.createWallet(createWalletRequestDto);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletService.getWalletInfo(walletId));
    }

    @Test
    public void failureOnNullWalletId() {
//        Arrange
        UUID walletId = null;

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class, () -> walletService.getWalletInfo(walletId));
    }

    @Test
    public void failureOnWalletNotFound() {
//        Arrange
        UUID walletId = UUID.randomUUID();

//        Act & Assert
        Assertions.assertThrows(EntityNotFoundException.class, () -> walletService.getWalletInfo(walletId));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        UUID walletId = walletService.createWallet(createWalletRequestDto);

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> walletService.getWalletInfo(walletId));
    }
}