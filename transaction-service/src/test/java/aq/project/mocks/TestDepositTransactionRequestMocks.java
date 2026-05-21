package aq.project.mocks;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.messages.TransactionRequest;

import java.math.BigDecimal;
import java.util.UUID;

import static aq.project.util.RequestPropertyKeys.RECIPIENT_PERSON_ID;
import static aq.project.util.RequestPropertyKeys.RECIPIENT_WALLET_ID;
import static aq.project.utils.UuidConstants.*;

public class TestDepositTransactionRequestMocks {

    public static TransactionRequest getValidDepositMessageRequest() {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.DEPOSIT,
                new BigDecimal("15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, STR_WALLET_ID);
        return transactionRequest;
    }

    public static TransactionRequest getValidDepositMessageRequest(String walletId, String personId) {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.DEPOSIT,
                new BigDecimal("15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, personId);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, walletId);
        return transactionRequest;
    }

    public static TransactionRequest getInvalidDepositMessageRequest() {
        TransactionRequest transactionRequest = new TransactionRequest(
                "abc",
                OperationType.DEPOSIT,
                new BigDecimal("-15.588"),
                "RUB",
                TransactionStatus.PENDING,
                -1L
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        return transactionRequest;
    }

    public static TransactionRequest getDepositMessageRequestWithUnknownUuid() {
        TransactionRequest transactionRequest = new TransactionRequest(
                UUID.randomUUID().toString(),
                OperationType.DEPOSIT,
                new BigDecimal("15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, UUID.randomUUID().toString());
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, UUID.randomUUID().toString());
        return transactionRequest;
    }

    public static TransactionRequest getInvalidDepositMessageRequestWithNegativeAmount() {
        TransactionRequest transactionRequest = new TransactionRequest(
                STR_TRANSACTION_ID,
                OperationType.DEPOSIT,
                new BigDecimal("-15.58"),
                "RUB",
                TransactionStatus.PENDING,
                Long.valueOf(System.currentTimeMillis())
        );
        transactionRequest.putProperty(RECIPIENT_PERSON_ID, STR_PERSON_ID);
        transactionRequest.putProperty(RECIPIENT_WALLET_ID, STR_WALLET_ID);
        return transactionRequest;
    }
}
