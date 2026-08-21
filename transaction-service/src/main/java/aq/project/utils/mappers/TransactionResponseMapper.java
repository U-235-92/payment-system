package aq.project.utils.mappers;

import aq.project.dto.EventType;
import aq.project.dto.TransactionStatusDto;
import aq.project.messages.TransactionResponse;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public abstract class TransactionResponseMapper {

    public static final TransactionResponseMapper INSTANCE = Mappers.getMapper(TransactionResponseMapper.class);

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "status", source = "transactionStatus")
    @Mapping(target = "eventType", expression = "java(toEventType(transactionResponse))")
    @Mapping(target = "notificationUrl", ignore = true)
    @Mapping(target = "description", ignore = true)
    public abstract TransactionStatusDto toTransactionStatusDto(TransactionResponse transactionResponse);

    protected EventType toEventType(TransactionResponse transactionResponse) {
        return switch (transactionResponse.getTransactionStatus()) {
            case COMPLETED -> EventType.TRANSACTION_COMPLETED;
            case FAILED -> EventType.TRANSACTION_FAILED;
            default -> throw new IllegalStateException(
                    String.format("Unexpected transaction status value: [%s]",
                            transactionResponse.getTransactionStatus().getValue().toLowerCase()));
        };
    }
}
