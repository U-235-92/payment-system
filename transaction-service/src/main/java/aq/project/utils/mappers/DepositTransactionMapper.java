package aq.project.utils.mappers;

import aq.project.dto.TransactionServiceDepositTransactionRequestDto;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.transaction_service.TransactionServiceTransactionMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DepositTransactionMapper {

    DepositTransactionMapper INSTANCE = Mappers.getMapper(DepositTransactionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "conversionRate", source = "conversionRate")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "transactionMetadata", expression = "java(toTransactionMetadata(dto))")
    TransactionServiceDepositTransaction toTransactionServiceDepositTransaction(
            TransactionServiceDepositTransactionRequestDto dto);

    default TransactionServiceTransactionMetadata toTransactionMetadata(
            TransactionServiceDepositTransactionRequestDto dto
    ) {
        TransactionServiceTransactionMetadata transactionServiceTransactionMetadata = new TransactionServiceTransactionMetadata();
        transactionServiceTransactionMetadata.setTraceId(dto.getTraceId());
        return transactionServiceTransactionMetadata;
    }
}
