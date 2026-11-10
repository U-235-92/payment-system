package aq.project.mappers;

import aq.project.dto.ConfirmTransactionRequestDto;
import aq.project.entities.Transaction;
import aq.project.kafka.events.DepositTransactionRequestEvent;
import aq.project.kafka.events.WithdrawalTransactionRequestEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

@Mapper
public interface TransactionMapper {

    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currencyCode")
    @Mapping(target = "timestamp", expression = "java(toTimestamp())")
    DepositTransactionRequestEvent toDepositTransactionRequestEvent(Transaction transaction);

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "targetWalletId", source = "targetWalletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currencyCode")
    @Mapping(target = "timestamp", expression = "java(toTimestamp())")
    WithdrawalTransactionRequestEvent toWithdrawalTransactionRequestEvent(Transaction transaction);

    default LocalDateTime toTimestamp() {
        return LocalDateTime.now();
    }

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "walletId", source = "walletId")
    @Mapping(target = "targetWalletId", source = "targetWalletId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currencyCode", source = "currencyCode")
    @Mapping(target = "paymentType", source = "paymentType")
    @Mapping(target = "transactionStatus", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "fee", ignore = true)
    Transaction toTransaction(ConfirmTransactionRequestDto dto);
}
