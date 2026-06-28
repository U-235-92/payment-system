package aq.project.util.mappers;

import aq.project.dto.TransactionRequestDTO;
import aq.project.messages.TransactionRequest;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class TransactionRequestMapper {

    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "timestamp", source = "timestamp")
    public abstract TransactionRequest toTransactionRequest(TransactionRequestDTO dto);

    @AfterMapping
    protected void copyProperties(@MappingTarget TransactionRequest transactionRequest, TransactionRequestDTO dto) {
        transactionRequest.copyProperties(dto.getProperties());
    }
}
