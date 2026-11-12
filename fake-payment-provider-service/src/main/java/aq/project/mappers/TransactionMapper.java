package aq.project.mappers;

import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.OperationType;
import aq.project.dto.TransactionInfoResponseDto;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "merchant", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currencyCode")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "description", source = "description")
    @Mapping(target = "externalId", source = "externalId")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    Transaction toTransaction(CreateTransactionRequestDto dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "merchantId", source = "merchant.merchantId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currency")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "externalId", source = "externalId")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    TransactionInfoResponseDto toTransactionInfoResponseDto(Transaction transaction);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "operationType", expression = "java(toOperationType())")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    UpdateStatusOperationRequestDto toUpdateStatusOperationRequestDto(Transaction transaction);

        default OperationType toOperationType() {
            return OperationType.TRANSACTION;
        }
}
