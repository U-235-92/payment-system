package aq.project._utils.entities;

import aq.project.dto.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class WalletServiceEntities {

    public static CreateWalletRequestDto getCreateWalletRequestDto() {
        return CreateWalletRequestDto.builder()
                .userId(UUID.randomUUID())
                .name("Name")
                .balance(BigDecimal.valueOf(85.58))
                .currencyCode("RUB")
                .userType(WalletUserType.PERSONAL)
                .creator("Creator")
                .modifier("Modifier")
                .walletStatus(WalletStatus.ACTIVE)
                .build();
    }

    public static DepositWalletRequestDto getDepositWalletRequestDto() {
        return DepositWalletRequestDto.builder()
                .walletId(UUID.randomUUID())
                .operationId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(85.58))
                .currencyCode("RUB")
                .build();
    }

    public static WithdrawalWalletRequestDto getWithdrawalWalletRequestDto() {
        return WithdrawalWalletRequestDto.builder()
                .walletId(UUID.randomUUID())
                .operationId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(85.58))
                .currencyCode("RUB")
                .build();
    }

    public static TransferWalletRequestDto getTransferWalletRequestDto() {
        return TransferWalletRequestDto.builder()
                .sourceWalletId(UUID.randomUUID())
                .targetWalletId(UUID.randomUUID())
                .operationId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(85.58))
                .targetAmount(BigDecimal.valueOf(85.58))
                .currencyCode("RUB")
                .build();
    }
}