package aq.project.services;

import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.entities.TransactionMetadata;
import aq.project.messages.TransactionRequest;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.PaymentServiceHandler;
import aq.project.utils.handlers.WalletServiceHandler;
import aq.project.utils.mappers.TransactionRequestMapper;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;

    private final PaymentServiceHandler paymentServiceHandler;
    private final WalletServiceHandler walletServiceHandler;

    private final TraceContext traceContext;

    private final TransactionRepository transactionRepository;

    public String sendTransactionRequest(TransactionRequest transactionRequest) {
        setUpReceivedTransactionRequest(transactionRequest);
        paymentServiceHandler.sendTransactionRequestToPaymentService(transactionRequest);
        saveTransactionRequest(transactionRequest);
        return transactionRequest.getTransactionId();
    }

    private void setUpReceivedTransactionRequest(TransactionRequest transactionRequest) {
        String transactionId = UUID.randomUUID().toString();
        transactionRequest.setTransactionId(transactionId);
        transactionRequest.setTransactionStatus(TransactionStatus.PENDING);
    }

    private void saveTransactionRequest(TransactionRequest transactionRequest) {
        String traceId = traceContext.getTraceId();

        TransactionMetadata transactionMetadata = new TransactionMetadata();
        transactionMetadata.setTraceId(traceId);
        transactionMetadata.setRetryCount(0);

        Transaction transaction = transactionRequestMapper.toTransaction(transactionRequest);
        transaction.setMetadata(transactionMetadata);

        transactionRepository.save(transaction);
    }

    public TransactionStatus getTransactionStatus(String transactionId) {
        return walletServiceHandler.getTransactionStatus(transactionId);
    }
}
