package aq.project._utils.entities;

import aq.project.dto.*;
import aq.project.entities.Transaction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TransactionServiceEntities {

    public static InitTransactionRequestDto getInitTransactionRequestDto() {
        return InitTransactionRequestDto.builder()
                .walletId(UUID.randomUUID())
                .targetWalletId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(85.58))
                .walletConversionRate(BigDecimal.valueOf(1.00))
                .targetWalletConversionRate(BigDecimal.valueOf(1.00))
                .paymentType(PaymentType.TRANSFER)
                .currencyCode("RUB")
                .build();
    }

    public static ConfirmTransactionRequestDto getConfirmTransactionRequestDto() {
        return ConfirmTransactionRequestDto.builder()
                .userId(UUID.randomUUID())
                .walletId(UUID.randomUUID())
                .targetWalletId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(85.58))
                .walletConversionRate(BigDecimal.valueOf(1.00))
                .targetWalletConversionRate(BigDecimal.valueOf(1.00))
                .paymentType(PaymentType.TRANSFER)
                .currencyCode("RUB")
                .build();
    }

    public static WalletInfoResponseDto getWalletInfoResponseDto() {
        return WalletInfoResponseDto.builder()
                .walletId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .name("Name")
                .balance(BigDecimal.valueOf(85.58))
                .currencyCode("RUB")
                .userType(WalletUserType.PERSONAL)
                .creator("Creator")
                .modifier("Modifier")
                .createdAt(OffsetDateTime.now())
                .modifiedAt(OffsetDateTime.now())
                .build();
    }

    public static Transaction getTransaction() {
        Transaction transaction = new Transaction();

        transaction.setId(UUID.randomUUID());
        transaction.setCreatedAt(OffsetDateTime.now());
        transaction.setModifiedAt(OffsetDateTime.now());
        transaction.setUserId(UUID.randomUUID());
        transaction.setWalletId(UUID.randomUUID());
        transaction.setTargetWalletId(UUID.randomUUID());
        transaction.setWalletAmount(BigDecimal.valueOf(85.58));
        transaction.setTargetWalletAmount(BigDecimal.valueOf(85.58));
        transaction.setCurrencyCode("RUB");
        transaction.setPaymentType(PaymentType.TRANSFER);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setDescription("Description");
        transaction.setFee(BigDecimal.ZERO);

        return transaction;
    }
}