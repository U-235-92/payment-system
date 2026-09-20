package aq.project.utils.mappers;

import aq.project.dto.TransactionServiceWithdrawTransactionRequestDto;
import aq.project.entities.transaction_service.TransactionServiceTransactionMetadata;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WithdrawTransactionMapper {

    WithdrawTransactionMapper INSTANCE = Mappers.getMapper(WithdrawTransactionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "conversionRate", source = "conversionRate")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "transactionMetadata", expression = "java(toTransactionMetadata(dto))")
    TransactionServiceWithdrawTransaction toTransactionServiceWithdrawTransaction(
            TransactionServiceWithdrawTransactionRequestDto dto);

    default TransactionServiceTransactionMetadata toTransactionMetadata(
            TransactionServiceWithdrawTransactionRequestDto dto
    ) {
        TransactionServiceTransactionMetadata transactionServiceTransactionMetadata = new TransactionServiceTransactionMetadata();
        transactionServiceTransactionMetadata.setTraceId(dto.getTraceId());
        return transactionServiceTransactionMetadata;
    }
}
