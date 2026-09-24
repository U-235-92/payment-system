package aq.project.utils.schedulers;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.CreateTransactionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CreateTransactionScheduler {

    private final TransactionRepository transactionRepository;

    private final CreateTransactionHandler createTransactionHandler;

    @Scheduled(
            fixedRateString = "${service.payment-provider-service.scheduler.handle_create_transaction.rate}",
            timeUnit = TimeUnit.MILLISECONDS
    )
    @Transactional
    public void handleCreateTransaction() {
        List<Transaction> transactions = transactionRepository.findAllByStatus(TransactionStatus.PENDING);

        for(Transaction transaction : transactions) {
            createTransactionHandler.handleScheduleCreateTransaction(transaction);
        }
    }
}
