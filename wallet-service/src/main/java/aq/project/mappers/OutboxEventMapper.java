package aq.project.mappers;

import aq.project.entities.OutboxEvent;
import aq.project.messages.TransactionRequest;
import aq.project.messages.TransactionResponse;
import org.mapstruct.*;

import static aq.project.util.RequestPropertyKeys.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class OutboxEventMapper {

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "transactionStatus", source = "transactionStatus")
    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "timestamp", source = "timestamp")
    public abstract OutboxEvent toOutboxEvent(TransactionRequest transactionRequest);

    @AfterMapping
    protected void toOutboxEventProperties(@MappingTarget OutboxEvent outboxEvent, TransactionRequest transactionRequest) {
        if(!transactionRequest.isPropertyNull(RECIPIENT_WALLET_ID))
            outboxEvent.putProperty(RECIPIENT_WALLET_ID, transactionRequest.getProperty(RECIPIENT_WALLET_ID, String.class));
        if(!transactionRequest.isPropertyNull(RECIPIENT_PERSON_ID))
            outboxEvent.putProperty(RECIPIENT_PERSON_ID, transactionRequest.getProperty(RECIPIENT_PERSON_ID, String.class));
        if(!transactionRequest.isPropertyNull(SENDER_WALLET_ID))
            outboxEvent.putProperty(SENDER_WALLET_ID, transactionRequest.getProperty(SENDER_WALLET_ID, String.class));
        if(!transactionRequest.isPropertyNull(SENDER_PERSON_ID))
            outboxEvent.putProperty(SENDER_PERSON_ID, transactionRequest.getProperty(SENDER_PERSON_ID, String.class));
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "transactionStatus", source = "transactionStatus")
    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "timestamp", source = "timestamp")
    public abstract TransactionResponse toResponseMessage(OutboxEvent outboxEvent);

    @AfterMapping
    protected void toResponseMessageProperties(@MappingTarget TransactionResponse transactionResponse, OutboxEvent outboxEvent) {
        if(!outboxEvent.isPropertyNull(RECIPIENT_WALLET_ID))
            transactionResponse.putProperty(RECIPIENT_WALLET_ID, outboxEvent.getProperty(RECIPIENT_WALLET_ID));
        if(!outboxEvent.isPropertyNull(RECIPIENT_PERSON_ID))
            transactionResponse.putProperty(RECIPIENT_PERSON_ID, outboxEvent.getProperty(RECIPIENT_PERSON_ID));
        if(!outboxEvent.isPropertyNull(SENDER_WALLET_ID))
            transactionResponse.putProperty(SENDER_WALLET_ID, outboxEvent.getProperty(SENDER_WALLET_ID));
        if(!outboxEvent.isPropertyNull(SENDER_PERSON_ID))
            transactionResponse.putProperty(SENDER_PERSON_ID, outboxEvent.getProperty(SENDER_PERSON_ID));
    }
}

