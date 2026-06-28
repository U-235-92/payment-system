package aq.project.util.handlers;

import aq.project.clients.CurrencyRateServiceClient;
import aq.project.dto.WalletStatus;
import aq.project.entities.Wallet;
import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.messages.TransactionRequest;
import aq.project.repositories.WalletRepository;
import aq.project.util.telemetry.TraceContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

import static aq.project.util.constants.RequestPropertyKeys.RECIPIENT_WALLET_ID;

@Component
public class DepositRequestHandler extends CommonRequestHandler {

    public DepositRequestHandler(
            CurrencyRateServiceClient currencyRateServiceClient,
            WalletRepository walletRepository,
            TransactionWriter transactionWriter,
            TraceContext traceContext
    ) {
        super(walletRepository, currencyRateServiceClient, transactionWriter, traceContext);
    }

    @Override
    protected void checkTransactionRequestPropertyConstrains(TransactionRequest transactionRequest) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        String walletId = transactionRequest.getProperty(RECIPIENT_WALLET_ID, String.class);
        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new NoSuchWalletException(walletId));
        if(wallet.getWalletDetails().getWalletStatus().equals(WalletStatus.BLOCKED))
            throw new WalletConstrainsException(String
                    .format("Wallet with id: [%s] is blocked",
                            wallet.getId()));
        if(wallet.getCreditCard().getCardExpirationDate().isBefore(YearMonth.now()))
            throw new CreditCardConstrainsException(String
                    .format("Credit card with number: [%s] of wallet with id: [%s] is expired",
                            wallet.getCreditCard().getCardNumber(), wallet.getId()));
    }

    @Override
    protected void handleTransactionRequestOperation(TransactionRequest transactionRequest) {
//        Prepare data
        String walletId = transactionRequest.getProperty(RECIPIENT_WALLET_ID, String.class);
        Wallet wallet = walletRepository.findByIdForUpdate(walletId).get();
//        Get conversion rate
        String sourceCurrency = transactionRequest.getCurrency();
        String destinationCurrency = wallet.getWalletDetails().getCurrencyCode();
        BigDecimal conversionRate = getConversionRate(sourceCurrency, destinationCurrency);
//        Execute operation
        BigDecimal currentBalance = wallet.getCreditCard().getBalance();
        BigDecimal convertedAmount = transactionRequest.getAmount().multiply(conversionRate);
        BigDecimal updatedBalance = currentBalance.add(convertedAmount);
        wallet.getCreditCard().setBalance(updatedBalance);
        wallet.getCreditCard().getInstantEmbeddedData().setUpdated(Instant.now());
    }
}
