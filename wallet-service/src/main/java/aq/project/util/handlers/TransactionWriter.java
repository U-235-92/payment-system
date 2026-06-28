package aq.project.util.handlers;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.repositories.TransactionRepository;
import aq.project.util.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TransactionWriter {

    private final TraceContext traceContext;

    @Transactional
    public void commitCompleteTransaction(TransactionRepository transactionRepository, Transaction transaction) {
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        transaction.setTraceId(traceContext.getTraceId());
        transactionRepository.save(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void commitFailedTransaction(TransactionRepository transactionRepository, Transaction transaction) {
        transaction.setTransactionStatus(TransactionStatus.FAILED);
        transaction.setTraceId(traceContext.getTraceId());
        transactionRepository.save(transaction);
    }
}
