package aq.project.util.request_handlers;

import aq.project.dto.WalletStatus;
import aq.project.entities.Wallet;
import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.messages.TransactionRequest;
import aq.project.repositories.WalletRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

import static aq.project.util.RequestPropertyKeys.RECIPIENT_WALLET_ID;

@Component
public class WithdrawRequestHandler extends OperationRequestHandler {

    private final WalletRepository walletRepository;

    public WithdrawRequestHandler(WalletRepository walletRepository) {
        super();
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    protected void checkRequestMessagePropertyConstrains(TransactionRequest transactionRequest) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        String walletId = transactionRequest.getProperty(RECIPIENT_WALLET_ID, String.class);
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new NoSuchWalletException(walletId));
        if(wallet.getWalletDetails().getWalletStatus().equals(WalletStatus.BLOCKED))
            throw new WalletConstrainsException(String
                    .format("Wallet with id: [%s] is blocked",
                            wallet.getId()));
        if(wallet.getCreditCard().getCardExpirationDate().isBefore(YearMonth.now()))
            throw new CreditCardConstrainsException(String
                    .format("Credit card with number: [%s] of wallet with id: [%s] is expired",
                            wallet.getCreditCard().getCardNumber(),
                            wallet.getId()));
        if(wallet.getCreditCard().getBalance().compareTo(transactionRequest.getAmount()) < 0)
            throw new CreditCardConstrainsException(String
                    .format("Credit card with number: [%s] has not enough money. Current balance: %s. Requested: %s",
                            wallet.getCreditCard().getCardNumber(),
                            wallet.getCreditCard().getBalance(),
                            transactionRequest.getAmount()));
    }

    @Override
    @Transactional
    protected void handleRequestMessageOperation(TransactionRequest transactionRequest) {
//        Prepare data
        String walletId = transactionRequest.getProperty(RECIPIENT_WALLET_ID, String.class);
        Wallet wallet = walletRepository.findById(walletId).get();
//        Execute operation
        BigDecimal currentBalance = wallet.getCreditCard().getBalance();
        BigDecimal updatedBalance = currentBalance.subtract(transactionRequest.getAmount());
        wallet.getCreditCard().setBalance(updatedBalance);
        wallet.getCreditCard().getInstantEmbeddedData().setUpdated(Instant.now());
    }
}
