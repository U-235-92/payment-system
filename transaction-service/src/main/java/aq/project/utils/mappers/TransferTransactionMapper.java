package aq.project.utils.mappers;

import aq.project.dto.TransactionServiceTransferTransactionRequestDto;
import aq.project.entities.transaction_service.TransactionServiceTransactionMetadata;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransferTransactionMapper {

    TransferTransactionMapper INSTANCE = Mappers.getMapper(TransferTransactionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderWalletId", source = "senderWalletId")
    @Mapping(target = "recipientWalletId", source = "recipientWalletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "senderConversionRate", source = "senderConversionRate")
    @Mapping(target = "recipientConversionRate", source = "recipientConversionRate")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "transactionMetadata", expression = "java(toTransactionMetadata(dto))")
    TransactionServiceTransferTransaction toTransactionServiceTransferTransaction(
            TransactionServiceTransferTransactionRequestDto dto);

    default TransactionServiceTransactionMetadata toTransactionMetadata(
            TransactionServiceTransferTransactionRequestDto dto
    ) {
        TransactionServiceTransactionMetadata transactionServiceTransactionMetadata = new TransactionServiceTransactionMetadata();
        transactionServiceTransactionMetadata.setTraceId(dto.getTraceId());
        return transactionServiceTransactionMetadata;
    }
}
