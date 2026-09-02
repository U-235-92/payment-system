package aq.project.utils.mappers.transaction;

import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.dto.DepositTransactionResponseWalletServiceDto;
import aq.project.dto.TransferTransactionResponseWalletServiceDto;
import aq.project.dto.WithdrawTransactionResponseWalletServiceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

@Mapper
public interface TransactionResponseMapper {

    TransactionResponseMapper INSTANCE = Mappers.getMapper(TransactionResponseMapper.class);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "transactionStatus", source = "status")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    DepositTransactionResponseWalletServiceDto toDepositResponse(DepositTransaction transaction);

    default OffsetDateTime toTimestamp(DepositTransaction transaction) {
        return transaction.getMetadata()
                .getTimestamp();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "transactionStatus", source = "status")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    WithdrawTransactionResponseWalletServiceDto toWithdrawResponse(WithdrawTransaction transaction);

    default OffsetDateTime toTimestamp(WithdrawTransaction transaction) {
        return transaction.getMetadata()
                .getTimestamp();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "senderWalletId", source = "senderWalletId")
    @Mapping(target = "recipientWalletId", source = "recipientWalletId")
    @Mapping(target = "transactionStatus", source = "status")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    TransferTransactionResponseWalletServiceDto toTransferResponse(TransferTransaction transaction);

    default OffsetDateTime toTimestamp(TransferTransaction transaction) {
        return transaction.getMetadata()
                .getTimestamp();
    }
}
