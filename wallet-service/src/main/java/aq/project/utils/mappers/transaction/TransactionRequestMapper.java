package aq.project.utils.mappers.transaction;

import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.transaction.TransactionMetadata;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.dto.DepositTransactionRequestDto;
import aq.project.dto.TransferTransactionRequestDto;
import aq.project.dto.WithdrawTransactionRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionRequestMapper {

    TransactionRequestMapper INSTANCE = Mappers.getMapper(TransactionRequestMapper.class);

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "status", source = "transactionStatus")
    @Mapping(target = "metadata", expression = "java(toMetadata(request))")
    DepositTransaction toDepositTransaction(DepositTransactionRequestDto request);

    default TransactionMetadata toMetadata(DepositTransactionRequestDto request) {
        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setTraceId(request.getTraceId());
        metadata.setTimestamp(request.getTimestamp());
        return metadata;
    }

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "status", source = "transactionStatus")
    @Mapping(target = "metadata", expression = "java(toMetadata(request))")
    WithdrawTransaction toWithdrawTransaction(WithdrawTransactionRequestDto request);

    default TransactionMetadata toMetadata(WithdrawTransactionRequestDto request) {
        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setTraceId(request.getTraceId());
        metadata.setTimestamp(request.getTimestamp());
        return metadata;
    }

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "senderWalletId", source = "senderWalletId")
    @Mapping(target = "recipientWalletId", source = "recipientWalletId")
    @Mapping(target = "status", source = "transactionStatus")
    @Mapping(target = "metadata", expression = "java(toMetadata(request))")
    TransferTransaction toTransferTransaction(TransferTransactionRequestDto request);

    default TransactionMetadata toMetadata(TransferTransactionRequestDto request) {
        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setTraceId(request.getTraceId());
        metadata.setTimestamp(request.getTimestamp());
        return metadata;
    }
}
