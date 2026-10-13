package aq.project.utils.mappers;

import aq.project.dto.IndividualsApiServiceDepositTransactionRequestDto;
import aq.project.dto.TransactionServiceDepositTransactionRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DepositTransactionMapper {

    DepositTransactionMapper INSTANCE = Mappers.getMapper(DepositTransactionMapper.class);

    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "traceId", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "conversionRate", ignore = true)
    @Mapping(target = "notificationUrl", ignore = true)
        TransactionServiceDepositTransactionRequestDto toTransactionServiceDepositTransactionRequestDto(
            IndividualsApiServiceDepositTransactionRequestDto transactionRequest);
}
