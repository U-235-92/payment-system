package aq.project.mappers;

import aq.project.dto.CreatePayoutRequestDto;
import aq.project.dto.OperationType;
import aq.project.dto.PayoutInfoResponseDto;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Payout;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayoutMapper {

    PayoutMapper INSTANCE = Mappers.getMapper(PayoutMapper.class);

    @Mapping(target = "merchant", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currencyCode")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "externalId", source = "externalId")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    Payout toPayout(CreatePayoutRequestDto createPayoutRequestDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "merchantId", source = "merchant.merchantId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currency")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "externalId", source = "externalId")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    PayoutInfoResponseDto toPayoutInfoResponseDto(Payout payout);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "operationType", expression = "java(toOperationType())")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    UpdateStatusOperationRequestDto toUpdateStatusOperationRequestDto(Payout payout);

    default OperationType toOperationType() {
        return OperationType.PAYOUT;
    }
}
