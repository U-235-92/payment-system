package aq.project.utils.mappers;

import aq.project.dto.*;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequest;
import aq.project.entities.payment_provider_service.PaymentProviderServiceTransactionRequestMetadata;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;

@Mapper
public interface PaymentProviderTransactionDtoMapper {

    PaymentProviderTransactionDtoMapper INSTANCE = Mappers.getMapper(PaymentProviderTransactionDtoMapper.class);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "operation", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "transactionRequestMetadata", expression = "java(toTransactionRequestMetadata(transaction))")
    PaymentProviderServiceTransactionRequest toPaymentProviderServiceTransactionRequest(
            TransactionServiceDepositTransaction transaction);

    default PaymentProviderServiceTransactionRequestMetadata toTransactionRequestMetadata(
            TransactionServiceDepositTransaction transaction
    ) {
        PaymentProviderServiceTransactionRequestMetadata metadata = new PaymentProviderServiceTransactionRequestMetadata();
        metadata.setTraceId(transaction.getTransactionMetadata().getTraceId());
        metadata.setTimestamp(transaction.getTransactionMetadata().getTimestamp());
        return metadata;
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "operation", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "transactionRequestMetadata", expression = "java(toTransactionRequestMetadata(transaction))")
    PaymentProviderServiceTransactionRequest toPaymentProviderServiceTransactionRequest(
            TransactionServiceWithdrawTransaction transaction);

    default PaymentProviderServiceTransactionRequestMetadata toTransactionRequestMetadata(
            TransactionServiceWithdrawTransaction transaction
    ) {
        PaymentProviderServiceTransactionRequestMetadata metadata = new PaymentProviderServiceTransactionRequestMetadata();
        metadata.setTraceId(transaction.getTransactionMetadata().getTraceId());
        metadata.setTimestamp(transaction.getTransactionMetadata().getTimestamp());
        return metadata;
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "operation", ignore = true)
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "transactionRequestMetadata", expression = "java(toTransactionRequestMetadata(transaction))")
    PaymentProviderServiceTransactionRequest toPaymentProviderServiceTransactionRequest(
            TransactionServiceTransferTransaction transaction);

    default PaymentProviderServiceTransactionRequestMetadata toTransactionRequestMetadata(
            TransactionServiceTransferTransaction transaction
    ) {
        PaymentProviderServiceTransactionRequestMetadata metadata = new PaymentProviderServiceTransactionRequestMetadata();
        metadata.setTraceId(transaction.getTransactionMetadata().getTraceId());
        metadata.setTimestamp(transaction.getTransactionMetadata().getTimestamp());
        return metadata;
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "operation", source = "operation")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "timestamp", expression = "java(toTimestamp(transaction))")
    PaymentProviderServiceCreateTransactionRequestDto toPaymentProviderServiceCreateTransactionRequestDto(
            PaymentProviderServiceTransactionRequest transaction);

    default String toTraceId(
            PaymentProviderServiceTransactionRequest transaction
    ) {
        return transaction.getTransactionRequestMetadata()
                .getTraceId();
    }

    default OffsetDateTime toTimestamp(
            PaymentProviderServiceTransactionRequest transaction
    ) {
        return transaction.getTransactionRequestMetadata()
                .getTimestamp();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceCancelTransactionRequestDto toPaymentProviderServiceCancelTransactionRequestDto(
            TransactionServiceDepositTransaction transaction);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            TransactionServiceDepositTransaction transaction);

    default String toTraceId(
            TransactionServiceDepositTransaction transaction
    ) {
        return transaction.getTransactionMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", expression = "java(toTraceId(transactionRequest))")
    @Mapping(target = "notificationUrl", ignore = true)
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            WalletServiceDepositTransactionRequest transactionRequest);

    default String toTraceId(
            WalletServiceDepositTransactionRequest transactionRequest
    ) {
        return transactionRequest.getTransactionRequestMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", expression = "java(toTraceId(transactionRequest))")
    @Mapping(target = "notificationUrl", ignore = true)
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            WalletServiceWithdrawTransactionRequest transactionRequest);

    default String toTraceId(
            WalletServiceWithdrawTransactionRequest transactionRequest
    ) {
        return transactionRequest.getTransactionRequestMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", expression = "java(toTraceId(transactionRequest))")
    @Mapping(target = "notificationUrl", ignore = true)
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            WalletServiceTransferTransactionRequest transactionRequest);

    default String toTraceId(
            WalletServiceTransferTransactionRequest transactionRequest
    ) {
        return transactionRequest.getTransactionRequestMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceCancelTransactionRequestDto toPaymentProviderServiceCancelTransactionRequestDto(
            TransactionServiceWithdrawTransaction transaction);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            TransactionServiceWithdrawTransaction transaction);

    default String toTraceId(
            TransactionServiceWithdrawTransaction transaction
    ) {
        return transaction.getTransactionMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceCancelTransactionRequestDto toPaymentProviderServiceCancelTransactionRequestDto(
            TransactionServiceTransferTransaction transaction);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", expression = "java(toTraceId(transaction))")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            TransactionServiceTransferTransaction transaction);

    default String toTraceId(
            TransactionServiceTransferTransaction transaction
    ) {
        return transaction.getTransactionMetadata()
                .getTraceId();
    }

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "notificationUrl", ignore = true)
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            WalletServiceDepositTransactionSuccessResponseDto dto);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "notificationUrl", ignore = true)
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            WalletServiceWithdrawTransactionSuccessResponseDto dto);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "notificationUrl", ignore = true)
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            WalletServiceTransferTransactionSuccessResponseDto dto);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", ignore = true)
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "notificationUrl", ignore = true)
    @Mapping(target = "operation", ignore = true)
    PaymentProviderServiceFailTransactionRequestDto toPaymentProviderServiceFailTransactionRequestDto(
            WalletServiceTransactionErrorResponseDto dto);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "operation", source = "operation")
    PaymentProviderServiceCancelTransactionRequestDto toPaymentProviderServiceCancelTransactionRequestDto(
            PaymentProviderServiceSuccessHandleTransactionDto dto);

    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "notificationUrl", source = "notificationUrl")
    @Mapping(target = "operation", source = "operation")
    PaymentProviderServiceCancelTransactionRequestDto toPaymentProviderServiceCancelTransactionRequestDto(
            PaymentProviderServiceErrorHandleTransactionDto dto);
}
