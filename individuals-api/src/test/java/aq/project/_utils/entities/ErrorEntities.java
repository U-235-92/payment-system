package aq.project._utils.entities;

import aq.project.dto.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ErrorEntities {

    public static PaymentServiceErrorDto getPaymentServiceErrorDto() {
        return new PaymentServiceErrorDto("TestPaymentServiceErrorDto");
    }

    public static CurrencyRateServiceErrorDto getCurrencyRateServiceErrorDto() {
        return new CurrencyRateServiceErrorDto("TestCurrencyRateServiceErrorDto");
    }

    public static PersonServiceErrorDto getPersonServiceErrorDto() {
        return new PersonServiceErrorDto("TestPersonServiceErrorDto");
    }

    public static TransactionServiceErrorDto getTransactionServiceErrorDto() {
        return new TransactionServiceErrorDto("TestTransactionServiceErrorDto");
    }

    public static WalletServiceErrorDto getWalletServiceErrorDto() {
        return new WalletServiceErrorDto("TestWalletServiceErrorDto");
    }
}
