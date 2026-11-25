package aq.project.test.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.WalletServiceEntities;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.DepositWalletRequestDto;
import aq.project.dto.WalletStatus;
import aq.project.exceptions.FallbackOperationException;
import aq.project.repositories.WalletRepository;
import aq.project.services.WalletService;
import jakarta.validation.ConstraintViolationException;
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
import java.util.UUID;

@Testcontainers
@DirtiesContext
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DepositWalletIT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL_CONTAINER;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @DynamicPropertySource
    static void setUpContainerProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer
                .registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @Test
    public void successDepositWallet() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(50));

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        DepositWalletRequestDto depositWalletRequestDto = WalletServiceEntities.getDepositWalletRequestDto();

        depositWalletRequestDto.setWalletId(walletId);
        depositWalletRequestDto.setAmount(BigDecimal.valueOf(30));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletService.depositWallet(depositWalletRequestDto));

        BigDecimal assertBalance = BigDecimal.valueOf(80);
        BigDecimal walletBalance = walletRepository.findById(walletId).get().getBalance();

        Assertions.assertTrue(assertBalance.compareTo(walletBalance) == 0);
    }

    @Test
    public void successOnDuplicateOperationRequest() {
//        Arrange
        UUID operationId = UUID.randomUUID();

        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(50));

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        DepositWalletRequestDto depositWalletRequestDto = WalletServiceEntities.getDepositWalletRequestDto();

        depositWalletRequestDto.setWalletId(walletId);
        depositWalletRequestDto.setOperationId(operationId);
        depositWalletRequestDto.setAmount(BigDecimal.valueOf(30));

        walletService.depositWallet(depositWalletRequestDto);
        walletService.depositWallet(depositWalletRequestDto);

        BigDecimal assertBalance = BigDecimal.valueOf(80);
        BigDecimal walletBalance = walletRepository.findById(walletId).get().getBalance();

        Assertions.assertTrue(assertBalance.compareTo(walletBalance) == 0);
    }

    @Test
    public void failureOnBlockedWallet() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setWalletStatus(WalletStatus.BLOCKED);

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        DepositWalletRequestDto depositWalletRequestDto = WalletServiceEntities.getDepositWalletRequestDto();

        depositWalletRequestDto.setWalletId(walletId);

//        Act & Assert
        Assertions.assertThrows(IllegalStateException.class,
                () -> walletService.depositWallet(depositWalletRequestDto));
    }

    @Test
    public void failureOnInvalidRequestDto() {
//        Arrange
        DepositWalletRequestDto depositWalletRequestDto = WalletServiceEntities.getDepositWalletRequestDto();

        depositWalletRequestDto.setWalletId(null);
        depositWalletRequestDto.setAmount(BigDecimal.valueOf(-10.00));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.depositWallet(depositWalletRequestDto));
    }

    @Test
    public void failureOnMismatchCurrencyCode() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(50));
        createWalletRequestDto.setCurrencyCode("RUB");

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        DepositWalletRequestDto depositWalletRequestDto = WalletServiceEntities.getDepositWalletRequestDto();

        depositWalletRequestDto.setWalletId(walletId);
        depositWalletRequestDto.setAmount(BigDecimal.valueOf(30));
        depositWalletRequestDto.setCurrencyCode("USD");

//        Act & Assert
        Assertions.assertThrows(IllegalStateException.class,
                () -> walletService.depositWallet(depositWalletRequestDto));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        DepositWalletRequestDto depositWalletRequestDto = WalletServiceEntities.getDepositWalletRequestDto();

        depositWalletRequestDto.setWalletId(walletId);

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> walletService.depositWallet(depositWalletRequestDto));
    }
}