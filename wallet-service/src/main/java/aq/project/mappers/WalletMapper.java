package aq.project.mappers;

import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.entities.Wallet;
import aq.project.entities.WalletType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WalletMapper {

    WalletMapper INSTANCE = Mappers.getMapper(WalletMapper.class);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "walletType", expression = "java(toWalletType(dto))")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "walletStatus", source = "walletStatus")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "archivedAt", ignore = true)
    Wallet toWallet(CreateWalletRequestDto dto);

    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "userType", source = "userType")
    @Mapping(target = "creator", source = "creator")
    @Mapping(target = "modifier", source = "modifier")
    WalletType toWalletType(CreateWalletRequestDto dto);

    @Mapping(target = "walletId", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "currencyCode", source = "walletType.currencyCode")
    @Mapping(target = "userType", source = "walletType.userType")
    @Mapping(target = "creator", source = "walletType.creator")
    @Mapping(target = "modifier", source = "walletType.modifier")
    @Mapping(target = "walletStatus", source = "walletStatus")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "modifiedAt", source = "modifiedAt")
    WalletInfoResponseDto toWalletInfoResponseDto(Wallet wallet);
}
