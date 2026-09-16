package aq.project.utils.mappers.transaction;

import aq.project.dto.*;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.transaction.WithdrawTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

@Mapper
public interface TransactionResponseMapper {

    TransactionResponseMapper INSTANCE = Mappers.getMapper(TransactionResponseMapper.class);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    WalletServiceDepositTransactionSuccessResponseDto toDepositResponse(DepositTransaction transaction);

    default OffsetDateTime toTimestamp(DepositTransaction transaction) {
        return transaction.getMetadata()
                .getTimestamp();
    }

    default String toTraceId(DepositTransaction transaction) {
        return transaction.getMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    WalletServiceWithdrawTransactionSuccessResponseDto toWithdrawResponse(
            WithdrawTransaction transaction);

    default OffsetDateTime toTimestamp(WithdrawTransaction transaction) {
        return transaction.getMetadata()
                .getTimestamp();
    }

    default String toTraceId(WithdrawTransaction transaction) {
        return transaction.getMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "senderWalletId", source = "senderWalletId")
    @Mapping(target = "recipientWalletId", source = "recipientWalletId")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    WalletServiceTransferTransactionSuccessResponseDto toTransferResponse(
            TransferTransaction transaction);

    default OffsetDateTime toTimestamp(TransferTransaction transaction) {
        return transaction.getMetadata()
                .getTimestamp();
    }

    default String toTraceId(TransferTransaction transaction) {
        return transaction.getMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "transactionStatus", ignore = true)
    @Mapping(target = "description", ignore = true)
    WalletServiceTransactionErrorResponseDto toWalletServiceTransactionResponseErrorDto(
            WalletServiceDepositTransactionRequestDto dto);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "transactionStatus", ignore = true)
    @Mapping(target = "description", ignore = true)
    WalletServiceTransactionErrorResponseDto toWalletServiceTransactionResponseErrorDto(
            WalletServiceTransferTransactionRequestDto dto);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "transactionStatus", ignore = true)
    @Mapping(target = "description", ignore = true)
    WalletServiceTransactionErrorResponseDto toWalletServiceTransactionResponseErrorDto(
            WalletServiceWithdrawTransactionRequestDto dto);
}
