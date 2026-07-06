package aq.project.util.handlers;

import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.messages.TransactionRequest;
import aq.project.repositories.TransactionRepository;
import aq.project.repositories.WalletRepository;
import aq.project.util.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CommonRequestHandler {

    protected final WalletRepository walletRepository;

    private final TransactionWriter transactionWriter;

    @Transactional
    public void handleRequestMessage(
            TransactionRequest transactionRequest,
            TransactionMapper transactionMapper,
            TransactionRepository transactionRepository
    ) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        try {
            checkTransactionRequestPropertyConstrains(transactionRequest);
            handleTransactionRequestOperation(transactionRequest);
            transactionWriter.commitCompleteTransaction(transactionRepository, transactionMapper.toTransaction(transactionRequest));
        } catch (WalletConstrainsException | CreditCardConstrainsException | NoSuchWalletException e) {
            transactionWriter.commitFailedTransaction(transactionRepository, transactionMapper.toTransaction(transactionRequest));
            throw e;
        }
    }

    protected void checkTransactionRequestPropertyConstrains(TransactionRequest transactionRequest) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        throw new UnsupportedOperationException();
    }

    protected void handleTransactionRequestOperation(TransactionRequest transactionRequest) {
        throw new UnsupportedOperationException();
    }
}
