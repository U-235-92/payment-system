package aq.project.utils.mappers.transaction;

import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.transaction.TransactionMetadata;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.dto.WalletServiceDepositTransactionRequestDto;
import aq.project.dto.WalletServiceTransferTransactionRequestDto;
import aq.project.dto.WalletServiceWithdrawTransactionRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionRequestMapper {

    TransactionRequestMapper INSTANCE = Mappers.getMapper(TransactionRequestMapper.class);

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "metadata", expression = "java(toMetadata(request))")
    DepositTransaction toDepositTransaction(WalletServiceDepositTransactionRequestDto request);

    default TransactionMetadata toMetadata(WalletServiceDepositTransactionRequestDto request) {
        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setTraceId(request.getTraceId());
        metadata.setTimestamp(request.getTimestamp());
        return metadata;
    }

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "metadata", expression = "java(toMetadata(request))")
    WithdrawTransaction toWithdrawTransaction(WalletServiceWithdrawTransactionRequestDto request);

    default TransactionMetadata toMetadata(WalletServiceWithdrawTransactionRequestDto request) {
        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setTraceId(request.getTraceId());
        metadata.setTimestamp(request.getTimestamp());
        return metadata;
    }

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "senderWalletId", source = "senderWalletId")
    @Mapping(target = "recipientWalletId", source = "recipientWalletId")
    @Mapping(target = "metadata", expression = "java(toMetadata(request))")
    TransferTransaction toTransferTransaction(WalletServiceTransferTransactionRequestDto request);

    default TransactionMetadata toMetadata(WalletServiceTransferTransactionRequestDto request) {
        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setTraceId(request.getTraceId());
        metadata.setTimestamp(request.getTimestamp());
        return metadata;
    }
}
