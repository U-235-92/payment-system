package aq.project.utils.mappers;

import aq.project.dto.MerchantRegistrationRequestDto;
import aq.project.dto.MerchantRegistrationResponseDto;
import aq.project.entities.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MerchantMapper {

    MerchantMapper INSTANCE = Mappers.getMapper(MerchantMapper.class);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "id", source = "merchantId")
    Merchant toMerchant(MerchantRegistrationRequestDto dto);

    MerchantRegistrationResponseDto toResponseDto(Merchant merchant);
}
