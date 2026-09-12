package aq.project.utils.handlers;

import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.repositories.transaction.TransferTransactionRepository;
import aq.project.repositories.transaction.WithdrawTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionHandler {

    private final TransferTransactionRepository transferTransactionRepository;
    private final DepositTransactionRepository depositTransactionRepository;
    private final WithdrawTransactionRepository withdrawTransactionRepository;

    @Transactional
    public void commitCompletedTransaction(DepositTransaction transaction) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        depositTransactionRepository.save(transaction);
    }

    @Transactional
    public void commitCompletedTransaction(WithdrawTransaction transaction) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        withdrawTransactionRepository.save(transaction);
    }

    @Transactional
    public void commitCompletedTransaction(TransferTransaction transaction) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transferTransactionRepository.save(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void commitFailedTransaction(DepositTransaction transaction) {
        transaction.setStatus(TransactionStatus.FAILED);
        depositTransactionRepository.save(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void commitFailedTransaction(WithdrawTransaction transaction) {
        transaction.setStatus(TransactionStatus.FAILED);
        withdrawTransactionRepository.save(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void commitFailedTransaction(TransferTransaction transaction) {
        transaction.setStatus(TransactionStatus.FAILED);
        transferTransactionRepository.save(transaction);
    }

    @Transactional
    public void checkDepositTransactionPresent(UUID transactionId) {
        if(depositTransactionRepository.findById(transactionId).isPresent())
            throw new DuplicateTransactionHandleException(String
                    .format("Attempt to handle duplicate of deposit transaction with id: [%s]", transactionId));
    }

    @Transactional
    public void checkWithdrawTransactionPresent(UUID transactionId) {
        if(withdrawTransactionRepository.findById(transactionId).isPresent())
            throw new DuplicateTransactionHandleException(String
                    .format("Attempt to handle duplicate of withdraw transaction with id: [%s]", transactionId));
    }

    @Transactional
    public void checkTransferTransactionPresent(UUID transactionId) {
        if(transferTransactionRepository.findById(transactionId).isPresent())
            throw new DuplicateTransactionHandleException(String
                    .format("Attempt to handle duplicate of transfer transaction with id: [%s]", transactionId));
    }
}
