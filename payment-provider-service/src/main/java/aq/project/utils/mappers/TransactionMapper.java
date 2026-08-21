package aq.project.utils.mappers;

import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionResponseDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.exceptions.AbsentPropertyException;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static aq.project.utils.constants.RequestPropertyKeys.TRANSACTION_ID;
import static aq.project.utils.constants.RequestPropertyKeys.TRANSACTION_STATUS;

@Mapper(builder = @Builder(disableBuilder = true))
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "id", expression = "java(toUuid(dto))")
    @Mapping(target = "status", expression = "java(toStatus(dto))")
    @Mapping(target = "merchant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "notificationUrl", ignore = true)
    Transaction toTransaction(TransactionRequestDto dto);

    default UUID toUuid(TransactionRequestDto dto) {
        if(dto.getProperties().containsKey(TRANSACTION_ID))
            return UUID.fromString(dto.getProperties().get(TRANSACTION_ID));
        throw new AbsentPropertyException("Received transaction request with no valid transaction ID value");
    }

    default TransactionStatus toStatus(TransactionRequestDto dto) {
        if(dto.getProperties().containsKey(TRANSACTION_STATUS))
            return TransactionStatus.valueOf(dto.getProperties().get(TRANSACTION_STATUS));
        throw new AbsentPropertyException("Received transaction request with no valid transaction status value");
    }

    @Mapping(source = "merchant.id", target = "merchantId")
    TransactionResponseDto toTransactionResponseDto(Transaction transaction);
}
