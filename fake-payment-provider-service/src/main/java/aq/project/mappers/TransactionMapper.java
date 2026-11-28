package aq.project.mappers;

import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.OperationType;
import aq.project.dto.TransactionInfoResponseDto;
import aq.project.dto.UpdateStatusOperationRequestDto;
import aq.project.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "merchant", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currencyCode")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "description", source = "description")
    @Mapping(target = "externalId", source = "externalId") // transactionId
    @Mapping(target = "paymentId", source = "paymentId")
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
    @Mapping(target = "transactionId", expression = "java(toTransactionId(transaction))")
    @Mapping(target = "paymentId", source = "paymentId")
    UpdateStatusOperationRequestDto toUpdateStatusOperationRequestDto(Transaction transaction);

    default String toOperationType() {
            return OperationType.TRANSACTION
                    .getValue();
    }

    default UUID toTransactionId(Transaction transaction) {
        String externalId = transaction.getExternalId();

        if(externalId == null)
            throw new NullPointerException("External id is null");

        return UUID.fromString(transaction.getExternalId());
    }
}