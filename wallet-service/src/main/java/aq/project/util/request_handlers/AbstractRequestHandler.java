package aq.project.util.request_handlers;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.mappers.TransactionMapper;
import aq.project.messages.TransactionRequest;
import aq.project.repositories.TransactionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class AbstractRequestHandler {

    protected AbstractRequestHandler() {
        super();
    }

    @Transactional
    public final void handleRequestMessage(TransactionRequest transactionRequest,
                                           TransactionMapper transactionMapper,
                                           TransactionRepository transactionRepository
    ) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        try {
            checkTransactionRequestPropertyConstrains(transactionRequest);
            handleTransactionRequestOperation(transactionRequest);
            commitTransaction(transactionRepository, transactionMapper.toTransaction(transactionRequest), TransactionStatus.COMPLETED);
        } catch (WalletConstrainsException | CreditCardConstrainsException | NoSuchWalletException e) {
            commitTransaction(transactionRepository, transactionMapper.toTransaction(transactionRequest), TransactionStatus.FAILED);
            throw e;
        }
    }

    private void commitTransaction(TransactionRepository transactionRepository,
                                   Transaction transaction,
                                   TransactionStatus transactionStatus
    ) {
        transaction.setTransactionStatus(transactionStatus);
        transactionRepository.save(transaction);
    }

    protected void checkTransactionRequestPropertyConstrains(TransactionRequest transactionRequest) throws WalletConstrainsException, CreditCardConstrainsException, NoSuchWalletException {
        throw new UnsupportedOperationException();
    }

    protected void handleTransactionRequestOperation(TransactionRequest transactionRequest) {
        throw new UnsupportedOperationException();
    }
}
