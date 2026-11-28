package aq.project._utils.entities;

import aq.project.dto.CreatePayoutRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PayoutServiceEntities {

    public static CreatePayoutRequestDto getCreatePayoutRequestDto() {
        CreatePayoutRequestDto createTransactionRequestDto = new CreatePayoutRequestDto();

        createTransactionRequestDto.setMerchantId(UUID.randomUUID().toString());
        createTransactionRequestDto.setAmount(BigDecimal.valueOf(85.58));
        createTransactionRequestDto.setCurrencyCode("RUB");
        createTransactionRequestDto.setExternalId(UUID.randomUUID().toString());
        createTransactionRequestDto.setPaymentId(UUID.randomUUID());
        createTransactionRequestDto.setNotificationUrl("https://example.aq");

        return createTransactionRequestDto;
    }
}