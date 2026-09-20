package aq.project._utils.entities.wallet_service;

import aq.project.dto.WalletServiceDepositTransactionSuccessResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class WalletServiceSuccessResponseCreateDepositTransactionHandlerEntities {

    public static WalletServiceDepositTransactionSuccessResponseDto getValidWalletServiceDepositTransactionSuccessResponseDto() {
        WalletServiceDepositTransactionSuccessResponseDto dto = new WalletServiceDepositTransactionSuccessResponseDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setWalletId(UUID.randomUUID());
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setTimestamp(OffsetDateTime.now());
        return dto;
    }

    public static WalletServiceDepositTransactionSuccessResponseDto getInvalidWalletServiceDepositTransactionSuccessResponseDto() {
        WalletServiceDepositTransactionSuccessResponseDto dto = new WalletServiceDepositTransactionSuccessResponseDto();
        dto.setTransactionId(null);
        dto.setWalletId(UUID.randomUUID());
        dto.setTraceId(null);
        dto.setTimestamp(OffsetDateTime.now());
        return dto;
    }
}
