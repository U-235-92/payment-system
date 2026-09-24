package aq.project.utils.mappers;

import aq.project.dto.IndividualsApiServiceTransferTransactionRequestDto;
import aq.project.dto.TransactionServiceTransferTransactionRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransferTransactionMapper {

    TransferTransactionMapper INSTANCE = Mappers.getMapper(TransferTransactionMapper.class);

    @Mapping(target = "senderWalletId", source = "senderWalletId")
    @Mapping(target = "recipientWalletId", source = "recipientWalletId")
    @Mapping(target = "traceId", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "senderConversionRate", ignore = true)
    @Mapping(target = "recipientConversionRate", ignore = true)
    @Mapping(target = "notificationUrl", ignore = true)
    TransactionServiceTransferTransactionRequestDto toTransactionServiceTransferTransactionRequestDto(
            IndividualsApiServiceTransferTransactionRequestDto transactionRequest);
}
