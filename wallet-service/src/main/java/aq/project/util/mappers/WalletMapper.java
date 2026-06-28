package aq.project.util.mappers;

import aq.project.dto.CardType;
import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.entities.CreditCard;
import aq.project.entities.InstantEmbeddedData;
import aq.project.entities.Wallet;
import aq.project.entities.WalletDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import static aq.project.util.constants.CustomConstants.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class WalletMapper {

    @Mapping(target = "personId", source = "personId")
    @Mapping(target = "creditCard", expression = "java(toCreditCard(dto))")
    @Mapping(target = "walletDetails", expression = "java(toWalletDetails(dto))")
    @Mapping(target = "instantEmbeddedData", expression = "java(toInstantEmbeddedData())")
    public abstract Wallet toWallet(CreateWalletRequestDTO dto);

    @Mapping(target = "cardNumber", source = "cardNumber")
    @Mapping(target = "cardCvvNumber", source = "cardCvvNumber")
    @Mapping(target = "cardType", source = "cardType")
    @Mapping(target = "cardExpirationDate", expression = "java(toExpirationDate(dto))")
    @Mapping(target = "balance", expression = "java(toBalance(dto))")
    @Mapping(target = "instantEmbeddedData", expression = "java(toInstantEmbeddedData())")
    protected abstract CreditCard toCreditCard(CreateWalletRequestDTO dto);

    protected YearMonth toExpirationDate(CreateWalletRequestDTO dto) {
        return (dto.getCardExpirationDate() == null)
                ? null
                : YearMonth.parse(dto.getCardExpirationDate(), DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT));
    }

    protected BigDecimal toBalance(CreateWalletRequestDTO dto) {
        return (dto.getBalance() == null)
                ? null
                : new BigDecimal(dto.getBalance());
    }

    @Mapping(target = "creator", source = "creator")
    @Mapping(target = "modifier", source = "modifier")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "walletStatus", source = "walletStatus")
    @Mapping(target = "instantEmbeddedData", expression = "java(toInstantEmbeddedData())")
    protected abstract WalletDetails toWalletDetails(CreateWalletRequestDTO dto);

    protected InstantEmbeddedData  toInstantEmbeddedData() {
        return new InstantEmbeddedData();
    }

    @Mapping(target = "personId", source = "personId")
    @Mapping(target = "walletId", source = "id")
    @Mapping(target = "createdAt", expression = "java(toCreatedAt(wallet))")
    @Mapping(target = "modifiedAt", expression = "java(toModifiedAt(wallet))")
    @Mapping(target = "currencyCode", expression = "java(toCurrencyCode(wallet))")
    @Mapping(target = "balance", expression = "java(toBalance(wallet))")
    @Mapping(target = "cardNumber", expression = "java(toCardNumber(wallet))")
    @Mapping(target = "cardType", expression = "java(toCardType(wallet))")
    @Mapping(target = "cardExpirationDate", expression = "java(toExpirationDate(wallet))")
    public abstract WalletInfoResponseDTO toWalletInfoResponseDto(Wallet wallet);

    protected String toCreatedAt(Wallet wallet) {
        return Instant.ofEpochMilli(wallet.getInstantEmbeddedData().getCreatedAt())
                .atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(ISO_DATE_FORMAT));
    }

    protected String toModifiedAt(Wallet wallet) {
        return Instant.ofEpochMilli(wallet.getInstantEmbeddedData().getUpdatedAt())
                .atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(ISO_DATE_FORMAT));
    }

    protected String toCurrencyCode(Wallet wallet) {
        return wallet.getWalletDetails().getCurrencyCode();
    }

    protected String toBalance(Wallet wallet) {
        return wallet.getCreditCard().getBalance().toString();
    }

    protected String toCardNumber(Wallet wallet) {
        return wallet.getCreditCard().getCardNumber();
    }

    protected CardType toCardType(Wallet wallet) {
        return wallet.getCreditCard().getCardType();
    }

    protected String toExpirationDate(Wallet wallet) {
        return wallet.getCreditCard().getCardExpirationDate().format(DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT));
    }
}
