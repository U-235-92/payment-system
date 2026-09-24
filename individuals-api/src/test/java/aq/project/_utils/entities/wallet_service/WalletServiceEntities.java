package aq.project._utils.entities.wallet_service;

import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class WalletServiceEntities {

    public static CreateWalletRequestDto getValidCreateWalletRequestDto() {
        CreateWalletRequestDto dto = new CreateWalletRequestDto();
        dto.setPersonId(UUID.randomUUID());
        dto.setWalletStatus(CreateWalletRequestDto.WalletStatusEnum.ACTIVE);
        dto.setCreator("creator");
        dto.setModifier("modifier");
        dto.setCurrencyCode("USD");
        dto.setBalance(BigDecimal.valueOf(100.00));
        dto.setCardNumber("0000 0000 0000 0000");
        dto.setCardCvvNumber("858");
        dto.setCardExpirationDate("05/85");
        dto.setCardType(CreateWalletRequestDto.CardTypeEnum.VISA);
        return dto;
    }

    public static CreateWalletRequestDto getInvalidCreateWalletRequestDto() {
        CreateWalletRequestDto dto = new CreateWalletRequestDto();
        dto.setPersonId(null);
        dto.setWalletStatus(CreateWalletRequestDto.WalletStatusEnum.ACTIVE);
        dto.setCreator(null);
        dto.setModifier(null);
        dto.setCurrencyCode("USD");
        dto.setBalance(BigDecimal.valueOf(-100.00));
        dto.setCardNumber("0000 0000 0000 0000");
        dto.setCardCvvNumber("858");
        dto.setCardExpirationDate("05/85");
        dto.setCardType(CreateWalletRequestDto.CardTypeEnum.VISA);
        return dto;
    }

    public static WalletInfoResponseDto getValidWalletInfoResponseDto() {
        WalletInfoResponseDto dto = new WalletInfoResponseDto();
        dto.setPersonId(UUID.randomUUID());
        dto.setWalletId(UUID.randomUUID());
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setModifiedAt(OffsetDateTime.now());
        dto.setBalance(BigDecimal.valueOf(100.00));
        dto.setCurrencyCode("USD");
        dto.setCardNumber("0000 0000 0000 0000");
        dto.setCardType(WalletInfoResponseDto.CardTypeEnum.VISA);
        dto.setCardExpirationDate("05/85");
        return dto;
    }

    public static WalletInfoResponseDto getInvalidWalletInfoResponseDto() {
        WalletInfoResponseDto dto = new WalletInfoResponseDto();
        dto.setPersonId(null);
        dto.setWalletId(null);
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setModifiedAt(OffsetDateTime.now());
        dto.setBalance(BigDecimal.valueOf(-100.00));
        dto.setCurrencyCode("USD");
        dto.setCardNumber("0000 0000 0000 0000");
        dto.setCardType(WalletInfoResponseDto.CardTypeEnum.VISA);
        dto.setCardExpirationDate("05/85");
        return dto;
    }
}
