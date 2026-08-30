package aq.project.utils.mappers;

import aq.project.dto.CreateTransactionRequestDto;
import aq.project.dto.TransactionResponseDto;
import aq.project.entities.Transaction;
import aq.project.entities.TransactionMetadata;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

@Mapper(builder = @Builder(disableBuilder = true))
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "merchant", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "operation", source = "operation")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "description", source = "description")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "metadata", expression = "java(toTransactionMetadata(dto))")
    Transaction toTransaction(CreateTransactionRequestDto dto);

    default TransactionMetadata toTransactionMetadata(CreateTransactionRequestDto dto) {
        TransactionMetadata transactionMetadata = new TransactionMetadata();
        transactionMetadata.setTraceId(dto.getTraceId());
        transactionMetadata.setCreatedAt(dto.getCreatedAt());
        transactionMetadata.setUpdatedAt(dto.getUpdatedAt());
        return transactionMetadata;
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", expression = "java(toMerchantId(transaction))")
    @Mapping(target = "operation", source = "operation")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", expression = "java(toCreatedAt(transaction))")
    @Mapping(target = "updatedAt", expression = "java(toUpdatedAt(transaction))")
    @Mapping(target = "description", source = "description")
    TransactionResponseDto toTransactionResponseDto(Transaction transaction);

    default String toMerchantId(Transaction transaction) {
        return transaction.getMerchant().getId();
    }

    default OffsetDateTime toCreatedAt(Transaction transaction) {
        return transaction.getMerchant().getCreatedAt();
    }

    default OffsetDateTime toUpdatedAt(Transaction transaction) {
        return transaction.getMerchant().getUpdatedAt();
    }
}
