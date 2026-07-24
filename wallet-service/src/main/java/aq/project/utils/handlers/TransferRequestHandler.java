package aq.project.utils.handlers;

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

import static aq.project.utils.constants.CustomConstants.DEPOSIT_OPERATION;
import static aq.project.utils.constants.CustomConstants.WITHDRAW_OPERATION;
import static aq.project.utils.constants.RequestPropertyKeys.*;

@Component
public class TransferRequestHandler extends CommonRequestHandler {

    public TransferRequestHandler(
            WalletRepository walletRepository,
            TransactionWriter transactionWriter
    ) {
        super(walletRepository, transactionWriter);
    }

    @Override
    @Transactional
    protected void checkTransactionRequestPropertyConstrains(TransactionRequest transactionRequest) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        String senderWalletId = transactionRequest.getProperty(SENDER_WALLET_ID);
        String recipientWalletId = transactionRequest.getProperty(RECIPIENT_WALLET_ID);
        checkRecipientWalletConstrains(recipientWalletId);
        checkSenderWalletConstrains(transactionRequest, senderWalletId);
    }

    private void checkRecipientWalletConstrains(String walletId) throws WalletConstrainsException, NoSuchWalletException, CreditCardConstrainsException {
        checkCommonWalletConstrains(walletId);
    }

    private void checkSenderWalletConstrains(TransactionRequest transactionRequest, String walletId) throws WalletConstrainsException, NoSuchWalletException, CreditCardConstrainsException {
        checkCommonWalletConstrains(walletId);
        Wallet wallet = walletRepository.findByIdForUpdate(walletId).get();
        if(wallet.getCreditCard().getBalance().compareTo(transactionRequest.getAmount()) < 0)
            throw new CreditCardConstrainsException(String
                    .format("Credit card with number: [%s] has not enough money. Current balance: %s. Requested: %s",
                            wallet.getCreditCard().getCardNumber(),
                            wallet.getCreditCard().getBalance(),
                            transactionRequest.getAmount()));
    }

    private void checkCommonWalletConstrains(String walletId) throws WalletConstrainsException, NoSuchWalletException, CreditCardConstrainsException {
        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
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
    }

    @Override
    protected void handleTransactionRequestOperation(TransactionRequest transactionRequest) {
//        Prepare data
        String recipientWalletId = transactionRequest.getProperty(RECIPIENT_WALLET_ID);
        Wallet recipientWallet = walletRepository.findByIdForUpdate(recipientWalletId).get();
        String senderWalletId = transactionRequest.getProperty(SENDER_WALLET_ID);
        Wallet senderWallet = walletRepository.findByIdForUpdate(senderWalletId).get();
//        Get conversion rates
        BigDecimal senderConversionRate = BigDecimal.valueOf(Double.valueOf(transactionRequest.getProperty(SENDER_CURRENCY_RATE)));
        BigDecimal recipientConversionRate = BigDecimal.valueOf(Double.valueOf(transactionRequest.getProperty(RECIPIENT_CURRENCY_RATE)));
//        Sender -> Transaction
        BigDecimal senderConvertedAmount = transactionRequest.getAmount().multiply(senderConversionRate);
//        Recipient -> Transaction
        BigDecimal recipientConvertedAmount = transactionRequest.getAmount().multiply(recipientConversionRate);
//        Execute operation
        doRequestMessageOperation(senderWallet, senderConvertedAmount, WITHDRAW_OPERATION);
        doRequestMessageOperation(recipientWallet, recipientConvertedAmount, DEPOSIT_OPERATION);
    }

    private void doRequestMessageOperation(Wallet wallet, BigDecimal amount, String operation) {
        BigDecimal currentBalance = wallet.getCreditCard().getBalance();
        BigDecimal updatedBalance = null;
        if(operation.equals(WITHDRAW_OPERATION))
            updatedBalance = currentBalance.subtract(amount);
        else if(operation.equals(DEPOSIT_OPERATION))
            updatedBalance = currentBalance.add(amount);
        else
            throw new IllegalArgumentException(String
                    .format("Unknown operation [%s] was called at %s.handleRequestMessageOperation()",
                            operation, TransferRequestHandler.class.getName()));
        wallet.getCreditCard().setBalance(updatedBalance);
        wallet.getCreditCard().getInstantEmbeddedData().setUpdated(Instant.now());
    }
}
