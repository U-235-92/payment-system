package aq.project._utils.entities.wallet_service;

import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceTransactionRequestMetadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class WalletServiceEntities {

    public static WalletServiceDepositTransactionRequest getValidWalletServiceDepositTransactionRequest() {
        WalletServiceTransactionRequestMetadata metadata = new WalletServiceTransactionRequestMetadata();
        metadata.setId(1L);
        metadata.setProcessed(false);
        metadata.setTimestamp(OffsetDateTime.now());
        metadata.setTraceId(UUID.randomUUID().toString());

        WalletServiceDepositTransactionRequest transactionRequest = new WalletServiceDepositTransactionRequest();
        transactionRequest.setTransactionId(UUID.randomUUID());
        transactionRequest.setWalletId(UUID.randomUUID());
        transactionRequest.setAmount(BigDecimal.valueOf(100.00));
        transactionRequest.setCurrencyCode("USD");
        transactionRequest.setConversionRate(BigDecimal.valueOf(1.00));
        transactionRequest.setTransactionRequestMetadata(metadata);
        return transactionRequest;
    }

    public static WalletServiceDepositTransactionRequest getInvalidWalletServiceDepositTransactionRequest() {
        WalletServiceTransactionRequestMetadata metadata = new WalletServiceTransactionRequestMetadata();
        metadata.setId(1L);
        metadata.setProcessed(false);
        metadata.setTimestamp(OffsetDateTime.now());
        metadata.setTraceId(null); // invalid null value

        WalletServiceDepositTransactionRequest transactionRequest = new WalletServiceDepositTransactionRequest();
        transactionRequest.setTransactionId(UUID.randomUUID());
        transactionRequest.setWalletId(null); // invalid null value
        transactionRequest.setAmount(BigDecimal.valueOf(-100.00)); // invalid negative amount value
        transactionRequest.setCurrencyCode("TEST"); // invalid currency code value
        transactionRequest.setConversionRate(BigDecimal.valueOf(1.00));
        transactionRequest.setTransactionRequestMetadata(metadata);
        return transactionRequest;
    }
}
