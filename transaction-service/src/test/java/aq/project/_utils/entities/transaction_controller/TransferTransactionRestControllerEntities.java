package aq.project._utils.entities.transaction_controller;

import aq.project.dto.TransactionServiceTransferTransactionRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TransferTransactionRestControllerEntities {

    public static TransactionServiceTransferTransactionRequestDto getValidTransactionServiceTransferTransactionRequestDto() {
        TransactionServiceTransferTransactionRequestDto dto = new TransactionServiceTransferTransactionRequestDto();
        dto.setRecipientWalletId(UUID.randomUUID());
        dto.setSenderWalletId(UUID.randomUUID());
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setCurrencyCode("USD");
        dto.setRecipientConversionRate(BigDecimal.valueOf(1.0));
        dto.setSenderConversionRate(BigDecimal.valueOf(1.0));
        dto.setNotificationUrl("https://www.example.aq");
        return dto;
    }

    public static TransactionServiceTransferTransactionRequestDto getInvalidTransactionServiceTransferTransactionRequestDto() {
        TransactionServiceTransferTransactionRequestDto dto = new TransactionServiceTransferTransactionRequestDto();
        dto.setRecipientWalletId(null);
        dto.setSenderWalletId(null);
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setAmount(BigDecimal.valueOf(-100.00));
        dto.setCurrencyCode(null);
        dto.setRecipientConversionRate(BigDecimal.valueOf(1.0));
        dto.setSenderConversionRate(BigDecimal.valueOf(1.0));
        dto.setNotificationUrl("https://www.example.aq");
        return dto;
    }
}
