package aq.project.utils.mappers.wallet;

import aq.project.dto.CardType;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.entities.wallet.WalletDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import static aq.project.utils.constants.CustomConstants.MONTH_YEAR_FORMAT;

@Mapper
public interface WalletMapper {

    WalletMapper INSTANCE = Mappers.getMapper(WalletMapper.class);

    @Mapping(target = "personId", source = "personId")
    @Mapping(target = "creditCard", expression = "java(toCreditCard(dto))")
    @Mapping(target = "walletDetails", expression = "java(toWalletDetails(dto))")
    Wallet toWallet(CreateWalletRequestDto dto);

    @Mapping(target = "cardNumber", source = "cardNumber")
    @Mapping(target = "cardCvvNumber", source = "cardCvvNumber")
    @Mapping(target = "cardType", source = "cardType")
    @Mapping(target = "cardExpirationDate", expression = "java(toExpirationDate(dto))")
    @Mapping(target = "balance", source = "balance")
    CreditCard toCreditCard(CreateWalletRequestDto dto);

    default YearMonth toExpirationDate(CreateWalletRequestDto dto) {
        return (dto.getCardExpirationDate() == null)
                ? null
                : YearMonth.parse(dto.getCardExpirationDate(), DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT));
    }

    @Mapping(target = "creator", source = "creator")
    @Mapping(target = "modifier", source = "modifier")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "walletStatus", source = "walletStatus")
    WalletDetails toWalletDetails(CreateWalletRequestDto dto);

    @Mapping(target = "personId", source = "personId")
    @Mapping(target = "walletId", source = "id")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "modifiedAt", source = "updatedAt")
    @Mapping(target = "currencyCode", expression = "java(toCurrencyCode(wallet))")
    @Mapping(target = "balance", expression = "java(toBalance(wallet))")
    @Mapping(target = "cardNumber", expression = "java(toCardNumber(wallet))")
    @Mapping(target = "cardType", expression = "java(toCardType(wallet))")
    @Mapping(target = "cardExpirationDate", expression = "java(toExpirationDate(wallet))")
    WalletInfoResponseDto toWalletInfoResponseDto(Wallet wallet);

    default String toCurrencyCode(Wallet wallet) {
        return wallet.getWalletDetails()
                .getCurrencyCode();
    }

    default BigDecimal toBalance(Wallet wallet) {
        return wallet.getCreditCard()
                .getBalance();
    }

    default String toCardNumber(Wallet wallet) {
        return wallet.getCreditCard()
                .getCardNumber();
    }

    default CardType toCardType(Wallet wallet) {
        return wallet.getCreditCard()
                .getCardType();
    }

    default String toExpirationDate(Wallet wallet) {
        return wallet.getCreditCard()
                .getCardExpirationDate()
                .format(DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT));
    }
}
