package aq.project._utils.entities.wallet_service;

import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceTransactionErrorResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class WalletServiceFailResponseCreateDepositTransactionHandlerEntities {

    public static WalletServiceTransactionErrorResponseDto getValidWalletServiceTransactionErrorResponseDto() {
        WalletServiceTransactionErrorResponseDto dto = new WalletServiceTransactionErrorResponseDto();
        dto.setTransactionId(UUID.randomUUID());
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setTransactionStatus(TransactionStatus.FAILED);
        return dto;
    }

    public static WalletServiceTransactionErrorResponseDto getInvalidWalletServiceTransactionErrorResponseDto() {
        WalletServiceTransactionErrorResponseDto dto = new WalletServiceTransactionErrorResponseDto();
        dto.setTransactionId(null);
        dto.setTraceId(null);
        dto.setTransactionStatus(TransactionStatus.FAILED);
        return dto;
    }
}
