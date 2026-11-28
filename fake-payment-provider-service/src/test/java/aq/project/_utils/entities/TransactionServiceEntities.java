package aq.project._utils.entities;

import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.PaymentMethod;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TransactionServiceEntities {

    public static CreateTransactionRequestDto getCreateTransactionRequestDto() {
        CreateTransactionRequestDto createTransactionRequestDto = new CreateTransactionRequestDto();

        createTransactionRequestDto.setMerchantId(UUID.randomUUID().toString());
        createTransactionRequestDto.setAmount(BigDecimal.valueOf(85.58));
        createTransactionRequestDto.setCurrencyCode("RUB");
        createTransactionRequestDto.setPaymentMethod(PaymentMethod.CARD);
        createTransactionRequestDto.setExternalId(UUID.randomUUID().toString());
        createTransactionRequestDto.setPaymentId(UUID.randomUUID());
        createTransactionRequestDto.setNotificationUrl("https://example.aq");
        createTransactionRequestDto.setDescription("Description");

        return createTransactionRequestDto;
    }
}