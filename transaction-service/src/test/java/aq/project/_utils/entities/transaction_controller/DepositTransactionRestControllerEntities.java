package aq.project._utils.entities.transaction_controller;

import aq.project.dto.TransactionServiceDepositTransactionRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class DepositTransactionRestControllerEntities {

    public static TransactionServiceDepositTransactionRequestDto getValidTransactionServiceDepositTransactionRequestDto() {
        TransactionServiceDepositTransactionRequestDto dto = new TransactionServiceDepositTransactionRequestDto();
        dto.setWalletId(UUID.randomUUID());
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setCurrencyCode("USD");
        dto.setConversionRate(BigDecimal.valueOf(1.0));
        dto.setNotificationUrl("https://www.example.aq");
        return dto;
    }

    public static TransactionServiceDepositTransactionRequestDto getInvalidTransactionServiceDepositTransactionRequestDto() {
        TransactionServiceDepositTransactionRequestDto dto = new TransactionServiceDepositTransactionRequestDto();
        dto.setWalletId(null);
        dto.setTraceId(UUID.randomUUID().toString());
        dto.setAmount(BigDecimal.valueOf(-100.00));
        dto.setCurrencyCode(null);
        dto.setConversionRate(BigDecimal.valueOf(1.0));
        dto.setNotificationUrl("https://www.example.aq");
        return dto;
    }
}
