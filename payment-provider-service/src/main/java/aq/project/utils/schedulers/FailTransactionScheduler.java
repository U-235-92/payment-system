package aq.project.utils.schedulers;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.TransactionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class FailTransactionScheduler {

    private final TransactionRepository transactionRepository;

    private final TransactionHandler transactionHandler;

    @Scheduled(
            fixedRateString = "${service.payment-provider-service.scheduler.handle_fail_transaction.rate}",
            timeUnit = TimeUnit.MILLISECONDS
    )
    public void handleFailTransaction() {
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.MARKED_FAILED);
        for(Transaction transaction : transactions) {
            transactionHandler.handleScheduleFailTransaction(transaction);
        }
    }
}
