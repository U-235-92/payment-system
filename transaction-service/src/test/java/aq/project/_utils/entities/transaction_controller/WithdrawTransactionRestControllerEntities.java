package aq.project._utils.entities.transaction_controller;

import aq.project.dto.TransactionServiceWithdrawTransactionRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class WithdrawTransactionRestControllerEntities {

    public static TransactionServiceWithdrawTransactionRequestDto getValidTransactionServiceWithdrawTransactionRequestDto() {
        TransactionServiceWithdrawTransactionRequestDto dto = new TransactionServiceWithdrawTransactionRequestDto();
        dto.setWalletId(UUID.randomUUID());
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setCurrencyCode("USD");
        dto.setConversionRate(BigDecimal.valueOf(1.0));
        dto.setNotificationUrl("https://www.example.aq");
        return dto;
    }

    public static TransactionServiceWithdrawTransactionRequestDto getInvalidTransactionServiceWithdrawTransactionRequestDto() {
        TransactionServiceWithdrawTransactionRequestDto dto = new TransactionServiceWithdrawTransactionRequestDto();
        dto.setWalletId(null);
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setAmount(BigDecimal.valueOf(-100.00));
        dto.setCurrencyCode(null);
        dto.setConversionRate(BigDecimal.valueOf(1.0));
        dto.setNotificationUrl("https://www.example.aq");
        return dto;
    }
}
