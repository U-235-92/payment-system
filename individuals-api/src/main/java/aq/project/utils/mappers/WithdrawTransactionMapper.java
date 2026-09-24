package aq.project.utils.mappers;

import aq.project.dto.IndividualsApiServiceWithdrawTransactionRequestDto;
import aq.project.dto.TransactionServiceWithdrawTransactionRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WithdrawTransactionMapper {

    WithdrawTransactionMapper INSTANCE = Mappers.getMapper(WithdrawTransactionMapper.class);

    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "traceId", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "conversionRate", ignore = true)
    @Mapping(target = "notificationUrl", ignore = true)
    TransactionServiceWithdrawTransactionRequestDto toTransactionServiceWithdrawTransactionRequestDto(
            IndividualsApiServiceWithdrawTransactionRequestDto transactionRequest);
}
