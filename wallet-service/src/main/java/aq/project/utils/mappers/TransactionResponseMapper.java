package aq.project.utils.mappers;

import aq.project.entities.DepositTransaction;
import aq.project.entities.TransferTransaction;
import aq.project.entities.WithdrawTransaction;
import aq.project.messages.responses.DepositTransactionResponse;
import aq.project.messages.responses.TransferTransactionResponse;
import aq.project.messages.responses.WithdrawTransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionResponseMapper {

    TransactionResponseMapper INSTANCE = Mappers.getMapper(TransactionResponseMapper.class);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "transactionStatus", source = "status")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    DepositTransactionResponse toDepositResponse(DepositTransaction transaction);

    default long toTimestamp(DepositTransaction transaction) {
        return transaction.getMetadata().getTimestamp();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "transactionStatus", source = "status")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    WithdrawTransactionResponse toWithdrawResponse(WithdrawTransaction transaction);

    default long toTimestamp(WithdrawTransaction transaction) {
        return transaction.getMetadata().getTimestamp();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "senderWalletId", source = "senderWalletId")
    @Mapping(target = "recipientWalletId", source = "recipientWalletId")
    @Mapping(target = "transactionStatus", source = "status")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    TransferTransactionResponse toTransferResponse(TransferTransaction transaction);

    default long toTimestamp(TransferTransaction transaction) {
        return transaction.getMetadata().getTimestamp();
    }
}
