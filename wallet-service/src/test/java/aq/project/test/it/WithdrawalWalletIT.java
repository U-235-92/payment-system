package aq.project.test.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.WalletServiceEntities;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletStatus;
import aq.project.dto.WithdrawalWalletRequestDto;
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
public class WithdrawalWalletIT {

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
    public void successWithdrawalWallet() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(80));

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        WithdrawalWalletRequestDto withdrawalWalletRequestDto = WalletServiceEntities.getWithdrawalWalletRequestDto();

        withdrawalWalletRequestDto.setWalletId(walletId);
        withdrawalWalletRequestDto.setAmount(BigDecimal.valueOf(50));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletService.withdrawalWallet(withdrawalWalletRequestDto));

        BigDecimal assertBalance = BigDecimal.valueOf(30);
        BigDecimal walletBalance = walletRepository.findById(walletId).get().getBalance();

        Assertions.assertTrue(assertBalance.compareTo(walletBalance) == 0);
    }

    @Test
    public void successOnDuplicateOperationRequest() {
//        Arrange
        UUID operationId = UUID.randomUUID();

        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(80));

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        WithdrawalWalletRequestDto withdrawalWalletRequestDto = WalletServiceEntities.getWithdrawalWalletRequestDto();

        withdrawalWalletRequestDto.setWalletId(walletId);
        withdrawalWalletRequestDto.setOperationId(operationId);
        withdrawalWalletRequestDto.setAmount(BigDecimal.valueOf(50));

        walletService.withdrawalWallet(withdrawalWalletRequestDto);
        walletService.withdrawalWallet(withdrawalWalletRequestDto);

        BigDecimal assertBalance = BigDecimal.valueOf(30);
        BigDecimal walletBalance = walletRepository.findById(walletId).get().getBalance();

        Assertions.assertTrue(assertBalance.compareTo(walletBalance) == 0);
    }

    @Test
    public void failureOnBlockedWallet() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setWalletStatus(WalletStatus.BLOCKED);

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        WithdrawalWalletRequestDto withdrawalWalletRequestDto = WalletServiceEntities.getWithdrawalWalletRequestDto();

        withdrawalWalletRequestDto.setWalletId(walletId);

//        Act & Assert
        Assertions.assertThrows(IllegalStateException.class,
                () -> walletService.withdrawalWallet(withdrawalWalletRequestDto));
    }

    @Test
    public void failureOnNotEnoughSufficient() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(0.00));

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        WithdrawalWalletRequestDto withdrawalWalletRequestDto = WalletServiceEntities.getWithdrawalWalletRequestDto();

        withdrawalWalletRequestDto.setWalletId(walletId);
        withdrawalWalletRequestDto.setAmount(BigDecimal.valueOf(85.00));

//        Act & Assert
        Assertions.assertThrows(IllegalStateException.class,
                () -> walletService.withdrawalWallet(withdrawalWalletRequestDto));
    }

    @Test
    public void failureOnInvalidRequestDto() {
//        Arrange
        WithdrawalWalletRequestDto withdrawalWalletRequestDto = WalletServiceEntities.getWithdrawalWalletRequestDto();

        withdrawalWalletRequestDto.setWalletId(null);
        withdrawalWalletRequestDto.setAmount(BigDecimal.valueOf(-10.00));

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.withdrawalWallet(withdrawalWalletRequestDto));
    }

    @Test
    public void failureOnMismatchCurrencyCode() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(80));
        createWalletRequestDto.setCurrencyCode("RUB");

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        WithdrawalWalletRequestDto withdrawalWalletRequestDto = WalletServiceEntities.getWithdrawalWalletRequestDto();

        withdrawalWalletRequestDto.setWalletId(walletId);
        withdrawalWalletRequestDto.setAmount(BigDecimal.valueOf(50));
        withdrawalWalletRequestDto.setCurrencyCode("USD");

//        Act & Assert
        Assertions.assertThrows(IllegalStateException.class,
                () -> walletService.withdrawalWallet(withdrawalWalletRequestDto));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        WithdrawalWalletRequestDto withdrawalWalletRequestDto = WalletServiceEntities.getWithdrawalWalletRequestDto();

        withdrawalWalletRequestDto.setWalletId(walletId);

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> walletService.withdrawalWallet(withdrawalWalletRequestDto));
    }
}