package aq.project.test.it;

import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project._utils.Containers;
import aq.project._utils.FallbackAssert;
import aq.project._utils.entities.WalletServiceEntities;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.TransferWalletRequestDto;
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
public class TransferWalletIT {

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

        createWalletRequestDto.setBalance(BigDecimal.valueOf(80.00));

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(50.00));

        UUID targetWalletId = walletService.createWallet(createWalletRequestDto);

        TransferWalletRequestDto transferWalletRequestDto = WalletServiceEntities.getTransferWalletRequestDto();

        transferWalletRequestDto.setTransferAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setTotalAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setSourceWalletId(walletId);
        transferWalletRequestDto.setTargetWalletId(targetWalletId);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> walletService.transferWallet(transferWalletRequestDto));

        BigDecimal walletAssertBalance = BigDecimal.valueOf(50.00);
        BigDecimal walletBalance = walletRepository.findById(walletId).get().getBalance();

        BigDecimal targetWalletAssertBalance = BigDecimal.valueOf(80.00);
        BigDecimal targetWalletBalance = walletRepository.findById(targetWalletId).get().getBalance();

        Assertions.assertTrue(walletAssertBalance.compareTo(walletBalance) == 0);
        Assertions.assertTrue(targetWalletAssertBalance.compareTo(targetWalletBalance) == 0);
    }

    @Test
    public void successOnDuplicateOperationRequest() {
//        Arrange
        UUID operationId = UUID.randomUUID();

        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(80.00));

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(50.00));

        UUID targetWalletId = walletService.createWallet(createWalletRequestDto);

        TransferWalletRequestDto transferWalletRequestDto = WalletServiceEntities.getTransferWalletRequestDto();

        transferWalletRequestDto.setOperationId(operationId);
        transferWalletRequestDto.setTransferAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setTotalAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setSourceWalletId(walletId);
        transferWalletRequestDto.setTargetWalletId(targetWalletId);

//        Act & Assert
        walletService.transferWallet(transferWalletRequestDto);
        walletService.transferWallet(transferWalletRequestDto);

        BigDecimal walletAssertBalance = BigDecimal.valueOf(50.00);
        BigDecimal walletBalance = walletRepository.findById(walletId).get().getBalance();

        BigDecimal targetWalletAssertBalance = BigDecimal.valueOf(80.00);
        BigDecimal targetWalletBalance = walletRepository.findById(targetWalletId).get().getBalance();

        Assertions.assertTrue(walletAssertBalance.compareTo(walletBalance) == 0);
        Assertions.assertTrue(targetWalletAssertBalance.compareTo(targetWalletBalance) == 0);
    }

    @Test
    public void failureOnBlockedWallet() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setWalletStatus(WalletStatus.BLOCKED);

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        UUID targetWalletId = walletService.createWallet(createWalletRequestDto);

        TransferWalletRequestDto transferWalletRequestDto = WalletServiceEntities.getTransferWalletRequestDto();

        transferWalletRequestDto.setTransferAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setTotalAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setSourceWalletId(walletId);
        transferWalletRequestDto.setTargetWalletId(targetWalletId);

//        Act & Assert
        Assertions.assertThrows(IllegalStateException.class,
                () -> walletService.transferWallet(transferWalletRequestDto));
    }

    @Test
    public void failureOnNotEnoughSufficient() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(0.00));

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(50.00));

        UUID targetWalletId = walletService.createWallet(createWalletRequestDto);

        TransferWalletRequestDto transferWalletRequestDto = WalletServiceEntities.getTransferWalletRequestDto();

        transferWalletRequestDto.setTransferAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setTotalAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setSourceWalletId(walletId);
        transferWalletRequestDto.setTargetWalletId(targetWalletId);

//        Act & Assert
        Assertions.assertThrows(IllegalStateException.class,
                () -> walletService.transferWallet(transferWalletRequestDto));
    }

    @Test
    public void failureOnInvalidRequestDto() {
//        Arrange
        TransferWalletRequestDto transferWalletRequestDto = WalletServiceEntities.getTransferWalletRequestDto();

        transferWalletRequestDto.setTotalAmount(BigDecimal.valueOf(-30.00));
        transferWalletRequestDto.setTransferAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setSourceWalletId(null);
        transferWalletRequestDto.setTargetWalletId(null);

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> walletService.transferWallet(transferWalletRequestDto));
    }

    @Test
    public void failureOnMismatchCurrencyCode() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(80.00));
        createWalletRequestDto.setCurrencyCode("RUB");

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(50.00));
        createWalletRequestDto.setCurrencyCode("EUR");

        UUID targetWalletId = walletService.createWallet(createWalletRequestDto);

        TransferWalletRequestDto transferWalletRequestDto = WalletServiceEntities.getTransferWalletRequestDto();

        transferWalletRequestDto.setTransferAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setTotalAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setSourceWalletId(walletId);
        transferWalletRequestDto.setTargetWalletId(targetWalletId);
        transferWalletRequestDto.setCurrencyCode("USD");

//        Act & Assert
        Assertions.assertThrows(IllegalStateException.class,
                () -> walletService.transferWallet(transferWalletRequestDto));
    }

    @Test
    public void failureOnRateLimitExceeded() {
//        Arrange
        CreateWalletRequestDto createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(80.00));

        UUID walletId = walletService.createWallet(createWalletRequestDto);

        createWalletRequestDto = WalletServiceEntities.getCreateWalletRequestDto();

        createWalletRequestDto.setBalance(BigDecimal.valueOf(50.00));

        UUID targetWalletId = walletService.createWallet(createWalletRequestDto);

        TransferWalletRequestDto transferWalletRequestDto = WalletServiceEntities.getTransferWalletRequestDto();

        transferWalletRequestDto.setTransferAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setTotalAmount(BigDecimal.valueOf(30.00));
        transferWalletRequestDto.setSourceWalletId(walletId);
        transferWalletRequestDto.setTargetWalletId(targetWalletId);

//        Act & Assert
        FallbackAssert.assertThrowsRateLimit(FallbackOperationException.class,
                () -> walletService.transferWallet(transferWalletRequestDto));
    }
}