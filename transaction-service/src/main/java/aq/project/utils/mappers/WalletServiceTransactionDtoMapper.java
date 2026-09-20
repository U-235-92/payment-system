package aq.project.utils.mappers;

import aq.project.dto.*;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceTransactionRequestMetadata;
import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

@Mapper
public interface WalletServiceTransactionDtoMapper {

    WalletServiceTransactionDtoMapper INSTANCE = Mappers.getMapper(WalletServiceTransactionDtoMapper.class);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "conversionRate", source = "conversionRate")
    @Mapping(target = "transactionRequestMetadata", expression = "java(toTransactionRequestMetadata(transaction))")
    WalletServiceDepositTransactionRequest toWalletServiceDepositTransactionRequest(
            TransactionServiceDepositTransaction transaction);

    default WalletServiceTransactionRequestMetadata toTransactionRequestMetadata(
            TransactionServiceDepositTransaction transaction
    ) {
        WalletServiceTransactionRequestMetadata metadata = new WalletServiceTransactionRequestMetadata();
        metadata.setTraceId(transaction.getTransactionMetadata().getTraceId());
        metadata.setProcessed(false);
        return metadata;
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "conversionRate", source = "conversionRate")
    @Mapping(target = "transactionRequestMetadata", expression = "java(toTransactionRequestMetadata(transaction))")
    WalletServiceWithdrawTransactionRequest toWalletServiceWithdrawTransactionRequest(
            TransactionServiceWithdrawTransaction transaction);

    default WalletServiceTransactionRequestMetadata toTransactionRequestMetadata(
            TransactionServiceWithdrawTransaction transaction
    ) {
        WalletServiceTransactionRequestMetadata metadata = new WalletServiceTransactionRequestMetadata();
        metadata.setTraceId(transaction.getTransactionMetadata().getTraceId());
        metadata.setProcessed(false);
        return metadata;
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "senderWalletId", source = "senderWalletId")
    @Mapping(target = "recipientWalletId", source = "recipientWalletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "senderConversionRate", source = "senderConversionRate")
    @Mapping(target = "recipientConversionRate", source = "recipientConversionRate")
    @Mapping(target = "transactionRequestMetadata", expression = "java(toTransactionRequestMetadata(transaction))")
    WalletServiceTransferTransactionRequest toWalletServiceTransferTransactionRequest(
            TransactionServiceTransferTransaction transaction);

    default WalletServiceTransactionRequestMetadata toTransactionRequestMetadata(
            TransactionServiceTransferTransaction transaction
    ) {
        WalletServiceTransactionRequestMetadata metadata = new WalletServiceTransactionRequestMetadata();
        metadata.setTraceId(transaction.getTransactionMetadata().getTraceId());
        metadata.setProcessed(false);
        return metadata;
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transactionRequest))")
    @Mapping(target = "traceId", expression = "java(toTraceId(transactionRequest))")
    @Mapping(target = "conversionRate", source = "conversionRate")
    WalletServiceDepositTransactionRequestDto toDepositTransactionRequestWalletServiceDto(
            WalletServiceDepositTransactionRequest transactionRequest);

    default OffsetDateTime toTimestamp(WalletServiceDepositTransactionRequest transactionRequest) {
        return transactionRequest.getTransactionRequestMetadata()
                .getTimestamp();
    }

    default String toTraceId(WalletServiceDepositTransactionRequest transactionRequest) {
        return transactionRequest.getTransactionRequestMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "senderWalletId", source = "senderWalletId")
    @Mapping(target = "recipientWalletId", source = "recipientWalletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transactionRequest))")
    @Mapping(target = "traceId", expression = "java(toTraceId(transactionRequest))")
    @Mapping(target = "senderConversionRate", source = "senderConversionRate")
    @Mapping(target = "recipientConversionRate", source = "recipientConversionRate")
    WalletServiceTransferTransactionRequestDto toTransferTransactionRequestWalletServiceDto(
            WalletServiceTransferTransactionRequest transactionRequest);

    default OffsetDateTime toTimestamp(WalletServiceTransferTransactionRequest transactionRequest) {
        return transactionRequest.getTransactionRequestMetadata()
                .getTimestamp();
    }

    default String toTraceId(WalletServiceTransferTransactionRequest transactionRequest) {
        return transactionRequest.getTransactionRequestMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transactionRequest))")
    @Mapping(target = "traceId", expression = "java(toTraceId(transactionRequest))")
    @Mapping(target = "conversionRate", source = "conversionRate")
    WalletServiceWithdrawTransactionRequestDto toWithdrawTransactionRequestWalletServiceDto(
            WalletServiceWithdrawTransactionRequest transactionRequest);

    default OffsetDateTime toTimestamp(WalletServiceWithdrawTransactionRequest transactionRequest) {
        return transactionRequest.getTransactionRequestMetadata()
                .getTimestamp();
    }

    default String toTraceId(WalletServiceWithdrawTransactionRequest transactionRequest) {
        return transactionRequest.getTransactionRequestMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    WalletServiceFailTransactionRequestDto toWalletServiceFailTransactionRequestDto(
            TransactionServiceDepositTransaction transaction);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "timestamp", source = "timestamp")
    WalletServiceFailTransactionRequestDto toWalletServiceFailTransactionRequestDto(
            WalletServiceDepositTransactionSuccessResponseDto dto);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    WalletServiceFailTransactionRequestDto toWalletServiceFailTransactionRequestDto(
            TransactionServiceWithdrawTransaction transaction);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "timestamp", source = "timestamp")
    WalletServiceFailTransactionRequestDto toWalletServiceFailTransactionRequestDto(
            WalletServiceWithdrawTransactionSuccessResponseDto dto);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    WalletServiceFailTransactionRequestDto toWalletServiceFailTransactionRequestDto(
            TransactionServiceTransferTransaction transaction);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "timestamp", source = "timestamp")
    WalletServiceFailTransactionRequestDto toWalletServiceFailTransactionRequestDto(
            WalletServiceTransferTransactionSuccessResponseDto dto);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    WalletServiceCancelTransactionRequestDto toWalletServiceDepositTransactionRequestDto(
            TransactionServiceDepositTransaction transaction);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    WalletServiceCancelTransactionRequestDto toWalletServiceWithdrawTransactionRequestDto(
            TransactionServiceWithdrawTransaction transaction);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    WalletServiceTransferTransactionRequestDto toWalletServiceTransferTransactionRequestDto(
            TransactionServiceTransferTransaction transaction);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    WalletServiceCancelTransactionRequestDto toWalletServiceCancelTransactionRequestDto(
            TransactionServiceTransferTransaction transaction);

    default String toTraceId(TransactionServiceDepositTransaction transaction) {
        return transaction.getTransactionMetadata()
                .getTraceId();
    }

    default OffsetDateTime toTimestamp(TransactionServiceDepositTransaction transaction) {
        return transaction.getTransactionMetadata()
                .getTimestamp();
    }

    default String toTraceId(TransactionServiceWithdrawTransaction transaction) {
        return transaction.getTransactionMetadata()
                .getTraceId();
    }

    default OffsetDateTime toTimestamp(TransactionServiceWithdrawTransaction transaction) {
        return transaction.getTransactionMetadata()
                .getTimestamp();
    }

    default String toTraceId(TransactionServiceTransferTransaction transaction) {
        return transaction.getTransactionMetadata()
                .getTraceId();
    }

    default OffsetDateTime toTimestamp(TransactionServiceTransferTransaction transaction) {
        return transaction.getTransactionMetadata()
                .getTimestamp();
    }
}
