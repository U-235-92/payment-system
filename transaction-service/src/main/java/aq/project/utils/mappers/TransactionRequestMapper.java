package aq.project.utils.mappers;

import aq.project.dto.TransactionRequestDto;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityMappingException;
import aq.project.messages.TransactionRequest;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import static aq.project.utils.constants.RequestPropertyKeys.TIMESTAMP;

@Mapper(builder = @Builder(disableBuilder = true))
public abstract class TransactionRequestMapper {

    public static final TransactionRequestMapper INSTANCE = Mappers.getMapper(TransactionRequestMapper.class);

    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(dto))")
    public abstract TransactionRequest toTransactionRequest(TransactionRequestDto dto);

    protected Long toTimestamp(TransactionRequestDto dto) {
        if(dto.getProperties().get(TIMESTAMP) != null && dto.getProperties().get(TIMESTAMP).matches("[0-9]+"))
            return Long.valueOf(dto.getProperties().get(TIMESTAMP));
        throw new EntityMappingException("Received transaction request with invalid timestamp value");
    }

    @AfterMapping
    protected void copyProperties(@MappingTarget TransactionRequest transactionRequest, TransactionRequestDto dto) {
        transactionRequest.copyProperties(dto.getProperties());
    }

    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    public abstract TransactionRequestDto toTransactionRequestDto(TransactionRequest transactionRequest);

    @AfterMapping
    protected void copyProperties(@MappingTarget TransactionRequestDto dto, TransactionRequest transactionRequest) {
        dto.getProperties().putAll(transactionRequest.getProperties());
    }

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "status", source = "transactionStatus")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "properties", source = "properties")
    public abstract Transaction toTransaction(TransactionRequest request);

    @AfterMapping
    protected void copyProperties(@MappingTarget Transaction transaction, TransactionRequest transactionRequest) {
        transaction.getProperties().putAll(transactionRequest.getProperties());
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "transactionStatus", source = "status")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "properties", source = "properties")
    public abstract TransactionRequest toTransactionRequest(Transaction transaction);

    @AfterMapping
    protected void copyProperties(@MappingTarget TransactionRequest transactionRequest, Transaction transaction) {
        transaction.getProperties().putAll(transactionRequest.getProperties());
    }
}
