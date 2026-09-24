package aq.project._utils.entities.deposit_transaction_service;

import aq.project.dto.IndividualsApiServiceDepositTransactionRequestDto;
import aq.project.dto.RateResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class DepositTransactionServiceEntities {

    public static IndividualsApiServiceDepositTransactionRequestDto getValidIndividualsApiServiceDepositTransactionRequestDto() {
        IndividualsApiServiceDepositTransactionRequestDto dto = new IndividualsApiServiceDepositTransactionRequestDto();
        dto.setWalletId(UUID.randomUUID());
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setCurrencyCode("USD");
        return dto;
    }

    public static IndividualsApiServiceDepositTransactionRequestDto getInvalidIndividualsApiServiceDepositTransactionRequestDto() {
        IndividualsApiServiceDepositTransactionRequestDto dto = new IndividualsApiServiceDepositTransactionRequestDto();
        dto.setWalletId(null);
        dto.setAmount(BigDecimal.valueOf(-100.00));
        dto.setCurrencyCode("TEST");
        return dto;
    }

    public static RateResponse getValidRateResponse() {
        RateResponse dto = new RateResponse();
        dto.setSourceCode("USD");
        dto.setDestinationCode("USD");
        dto.setRate(BigDecimal.valueOf(1.0));
        dto.setRateDate(OffsetDateTime.now());
        dto.setProviderCode("AMCM");
        return dto;
    }

    public static RateResponse getInvalidRateResponse() {
        RateResponse dto = new RateResponse();
        dto.setSourceCode("USD");
        dto.setDestinationCode(null);
        dto.setRate(BigDecimal.valueOf(-1.0));
        dto.setRateDate(OffsetDateTime.now());
        dto.setProviderCode("AMCM");
        return dto;
    }
}
