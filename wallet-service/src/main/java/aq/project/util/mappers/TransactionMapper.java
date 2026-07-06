package aq.project.util.mappers;

import aq.project.entities.Transaction;
import aq.project.messages.TransactionRequest;
import aq.project.messages.TransactionResponse;
import org.mapstruct.*;

import static aq.project.util.constants.RequestPropertyKeys.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class TransactionMapper {

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "transactionStatus", source = "transactionStatus")
    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "timestamp", source = "timestamp")
    public abstract Transaction toTransaction(TransactionRequest transactionRequest);

    @AfterMapping
    protected void toTransactionProperties(@MappingTarget Transaction transaction, TransactionRequest transactionRequest) {
        if(!transactionRequest.isPropertyNull(RECIPIENT_WALLET_ID))
            transaction.putProperty(RECIPIENT_WALLET_ID, transactionRequest.getProperty(RECIPIENT_WALLET_ID));

        if(!transactionRequest.isPropertyNull(RECIPIENT_PERSON_ID))
            transaction.putProperty(RECIPIENT_PERSON_ID, transactionRequest.getProperty(RECIPIENT_PERSON_ID));

        if(!transactionRequest.isPropertyNull(SENDER_WALLET_ID))
            transaction.putProperty(SENDER_WALLET_ID, transactionRequest.getProperty(SENDER_WALLET_ID));

        if(!transactionRequest.isPropertyNull(SENDER_PERSON_ID))
            transaction.putProperty(SENDER_PERSON_ID, transactionRequest.getProperty(SENDER_PERSON_ID));
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "transactionStatus", source = "transactionStatus")
    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "timestamp", source = "timestamp")
    public abstract TransactionResponse toTransactionResponse(Transaction transaction);

    @AfterMapping
    protected void toTransactionResponseProperties(@MappingTarget TransactionResponse transactionResponse, Transaction transaction) {
        if(!transaction.isPropertyNull(RECIPIENT_WALLET_ID))
            transactionResponse.putProperty(RECIPIENT_WALLET_ID, transaction.getProperty(RECIPIENT_WALLET_ID));

        if(!transaction.isPropertyNull(RECIPIENT_PERSON_ID))
            transactionResponse.putProperty(RECIPIENT_PERSON_ID, transaction.getProperty(RECIPIENT_PERSON_ID));

        if(!transaction.isPropertyNull(SENDER_WALLET_ID))
            transactionResponse.putProperty(SENDER_WALLET_ID, transaction.getProperty(SENDER_WALLET_ID));

        if(!transaction.isPropertyNull(SENDER_PERSON_ID))
            transactionResponse.putProperty(SENDER_PERSON_ID, transaction.getProperty(SENDER_PERSON_ID));
    }
}

