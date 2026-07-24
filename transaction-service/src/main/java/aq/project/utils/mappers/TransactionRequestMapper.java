package aq.project.utils.mappers;

import aq.project.dto.TransactionRequestDto;
import aq.project.messages.TransactionRequest;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class TransactionRequestMapper {

    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "timestamp", source = "timestamp")
    public abstract TransactionRequest toTransactionRequest(TransactionRequestDto dto);

    @AfterMapping
    protected void copyProperties(@MappingTarget TransactionRequest transactionRequest, TransactionRequestDto dto) {
        transactionRequest.copyProperties(dto.getProperties());
    }
}
