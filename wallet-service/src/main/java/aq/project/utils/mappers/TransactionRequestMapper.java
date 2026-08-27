package aq.project.utils.mappers;

import aq.project.entities.DepositTransaction;
import aq.project.entities.TransactionMetadata;
import aq.project.entities.TransferTransaction;
import aq.project.entities.WithdrawTransaction;
import aq.project.messages.requests.DepositTransactionRequest;
import aq.project.messages.requests.TransferTransactionRequest;
import aq.project.messages.requests.WithdrawTransactionRequest;
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
    DepositTransaction toDepositTransaction(DepositTransactionRequest request);

    default TransactionMetadata toMetadata(DepositTransactionRequest request) {
        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setTraceId(request.getTraceId());
        metadata.setTimestamp(request.getTimestamp());
        return metadata;
    }

    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "status", source = "transactionStatus")
    @Mapping(target = "metadata", expression = "java(toMetadata(request))")
    WithdrawTransaction toWithdrawTransaction(WithdrawTransactionRequest request);

    default TransactionMetadata toMetadata(WithdrawTransactionRequest request) {
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
    TransferTransaction toTransferTransaction(TransferTransactionRequest request);

    default TransactionMetadata toMetadata(TransferTransactionRequest request) {
        TransactionMetadata metadata = new TransactionMetadata();
        metadata.setTraceId(request.getTraceId());
        metadata.setTimestamp(request.getTimestamp());
        return metadata;
    }
}
