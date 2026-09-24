package aq.project._utils.entities.transfer_transaction_service;

import aq.project.dto.IndividualsApiServiceTransferTransactionRequestDto;
import aq.project.dto.RateResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TransferTransactionServiceEntities {
    
    public static IndividualsApiServiceTransferTransactionRequestDto getValidIndividualsApiServiceTransferTransactionRequestDto() {
        IndividualsApiServiceTransferTransactionRequestDto dto = new IndividualsApiServiceTransferTransactionRequestDto();
        dto.setSenderWalletId(UUID.randomUUID());
        dto.setRecipientWalletId(UUID.randomUUID());
        dto.setAmount(BigDecimal.valueOf(100.00));
        dto.setCurrencyCode("USD");
        return dto;
    }

    public static IndividualsApiServiceTransferTransactionRequestDto getInvalidIndividualsApiServiceTransferTransactionRequestDto() {
        IndividualsApiServiceTransferTransactionRequestDto dto = new IndividualsApiServiceTransferTransactionRequestDto();
        dto.setSenderWalletId(null);
        dto.setRecipientWalletId(null);
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
