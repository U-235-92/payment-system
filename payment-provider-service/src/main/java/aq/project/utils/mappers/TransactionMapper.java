package aq.project.utils.mappers;

import aq.project.dto.CreateTransactionRequestPaymentProviderServiceDto;
import aq.project.dto.TransactionResponsePaymentProviderServiceDto;
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
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "operation", source = "operation")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "description", source = "description")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "metadata", expression = "java(toTransactionMetadata(dto))")
    Transaction toTransaction(CreateTransactionRequestPaymentProviderServiceDto dto);

    default TransactionMetadata toTransactionMetadata(CreateTransactionRequestPaymentProviderServiceDto dto) {
        TransactionMetadata transactionMetadata = new TransactionMetadata();
        transactionMetadata.setTraceId(dto.getTraceId());
        transactionMetadata.setTimestamp(dto.getTimestamp());
        return transactionMetadata;
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", expression = "java(toMerchantId(transaction))")
    @Mapping(target = "operation", source = "operation")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "transactionStatus", source = "status")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    @Mapping(target = "description", source = "description")
    TransactionResponsePaymentProviderServiceDto toTransactionResponseDto(Transaction transaction);

    default String toMerchantId(Transaction transaction) {
        return transaction.getMerchant()
                .getId();
    }

    default OffsetDateTime toTimestamp(Transaction transaction) {
        return transaction.getMetadata()
                .getTimestamp();
    }
}
