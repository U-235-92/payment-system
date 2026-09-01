package aq.project.services.transaction;

import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.messages.requests.TransferTransactionRequest;
import aq.project.messages.responses.TransferTransactionResponse;
import aq.project.repositories.transaction.TransferTransactionRepository;
import aq.project.services.wallet.WalletService;
import aq.project.utils.handlers.TransactionHandler;
import aq.project.utils.mappers.transaction.TransactionRequestMapper;
import aq.project.utils.mappers.transaction.TransactionResponseMapper;
import aq.project.utils.telemetry.TraceContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferTransactionService {

    private static final String DEPOSIT = "deposit";
    private static final String WITHDRAW = "withdraw";

    @Value("${spring.application.name}")
    private String tracerName;

    @Value("${service.kafka.topics.transfer_transaction_response.name}")
    private String transactionResponseTopicName;

    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;
    private final TransactionResponseMapper transactionResponseMapper = TransactionResponseMapper.INSTANCE;

    private final TransactionHandler transactionHandler;

    private final TraceContext traceContext;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OpenTelemetry openTelemetry;

    private final TransferTransactionRepository transferTransactionRepository;

    private final WalletService walletService;

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.transfer_transaction_request.name}")
    public void handleTransactionRequest(TransferTransactionRequest request) {
        TransferTransaction transaction = transactionRequestMapper.toTransferTransaction(request);
        try {
            traceContext.clean();
            traceContext.setTraceId(request.getTraceId());

            checkIdempotentTransactionHandle(request);
            checkTransactionConstraints(request);
            handleTransaction(request);

            transactionHandler.commitCompletedTransaction(transaction);
        } catch(EntityNotFoundException | DuplicateTransactionHandleException | EntityConstraintsException e) {
            transactionHandler.commitFailedTransaction(transaction);
            throw e;
        } finally {
            traceContext.clean();
        }
    }

    private void checkIdempotentTransactionHandle(TransferTransactionRequest request) {
        UUID transactionId = request.getTransactionId();
        transactionHandler.checkTransferTransactionPresent(transactionId);
    }

    private void checkTransactionConstraints(TransferTransactionRequest request) {
        UUID senderWalletId = request.getSenderWalletId();
        UUID recipientWalletId = request.getRecipientWalletId();

        Wallet senderWallet = walletService.getWallet(senderWalletId);
        Wallet recipientWallet = walletService.getWallet(recipientWalletId);

        CreditCard senderCreditCard = senderWallet.getCreditCard();
        CreditCard recipientCreditCard = recipientWallet.getCreditCard();

        if(walletService.isWalletBlocked(senderWallet))
            throw new EntityConstraintsException(String
                    .format("Wallet with id: [%s] is blocked", senderWallet.getId()));

        if(walletService.isWalletBlocked(recipientWallet))
            throw new EntityConstraintsException(String
                    .format("Wallet with id: [%s] is blocked", recipientWallet.getId()));

        if(walletService.isWalletCreditCardExpired(senderCreditCard))
            throw new EntityConstraintsException(String
                    .format("Credit card with number: [%s] of wallet with id: [%s] is expired",
                            senderCreditCard.getCardNumber(), senderWalletId));

        if(walletService.isWalletCreditCardExpired(recipientCreditCard))
            throw new EntityConstraintsException(String
                    .format("Credit card with number: [%s] of wallet with id: [%s] is expired",
                            recipientCreditCard.getCardNumber(), recipientWalletId));

        if(walletService.isWalletCreditCardBalanceLessThan(senderCreditCard, request.getAmount()))
            throw new EntityConstraintsException(String
                    .format("Credit card with number: [%s] has not enough money. Current balance: %s. Requested: %s",
                            senderCreditCard.getCardNumber(), senderCreditCard.getBalance(), request.getAmount()));
    }

    private void handleTransaction(TransferTransactionRequest request) {
        UUID recipientWalletId = request.getRecipientWalletId();
        Wallet recipientWallet = walletService.getWalletWithLock(recipientWalletId);

        UUID senderWalletId = request.getSenderWalletId();
        Wallet senderWallet = walletService.getWalletWithLock(senderWalletId);

        BigDecimal senderConversionRate = request.getSenderConversionRate();
        BigDecimal senderConvertedAmount = request.getAmount().multiply(senderConversionRate);

        BigDecimal recipientConversionRate = request.getRecipientConversionRate();
        BigDecimal recipientConvertedAmount = request.getAmount().multiply(recipientConversionRate);

        handleTransaction(senderWallet, senderConvertedAmount, WITHDRAW);
        handleTransaction(recipientWallet, recipientConvertedAmount, DEPOSIT);
    }

    private void handleTransaction(Wallet wallet, BigDecimal amount, String operation) {
        BigDecimal currentBalance = wallet.getCreditCard().getBalance();
        BigDecimal updatedBalance = null;
        switch (operation) {
            case WITHDRAW -> updatedBalance = currentBalance.subtract(amount);
            case DEPOSIT -> updatedBalance = currentBalance.add(amount);
        }
        wallet.getCreditCard().setBalance(updatedBalance);
    }

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void handleTransaction() {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("handle_transfer_transaction").startSpan();
        try(Scope scope = span.makeCurrent()) {
            int pageNumber = 0, pageSize = 100;
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("metadata.timestamp").descending());
            List<TransferTransaction> transferTransactions = transferTransactionRepository.findAll(pageable).getContent();
            for(TransferTransaction transaction : transferTransactions) {
                try {
                    handleTransaction(transaction, span);
                } catch (Exception e) {
                    String traceId = transaction.getMetadata().getTraceId();
                    String spanId = span.getSpanContext().getSpanId();

                    log.warn("[{}-{}]: Error occurred while handle transfer transaction with id: [{}]",
                            traceId, spanId, transaction.getId());
                }
            }
        } finally {
            span.end();
        }
    }

    private void handleTransaction(TransferTransaction transaction, Span span) throws Exception {
        String traceId = transaction.getMetadata().getTraceId();
        String spanId = span.getSpanContext().getSpanId();

        log.info("[{}-{}]: Attempt to handle transfer transaction with id: [{}]",
                traceId, spanId, transaction.getId());

        TransferTransactionResponse response = transactionResponseMapper.toTransferResponse(transaction);

        ProducerRecord<String, TransferTransactionResponse> record = new ProducerRecord<>(transactionResponseTopicName, response);

        Headers headers = record.headers();
        headers.add(X_TRACE_ID_HEADER, traceId.getBytes());

        kafkaTemplate.send((ProducerRecord) record).get();

        transaction.setProcessed(true);

        log.info("[{}-{}]: Handle of transfer transaction with id: [{}] completed",
                traceId, spanId, transaction.getId());
    }

    public TransactionStatus getTransactionStatus(UUID transactionId) {
        TransferTransaction transaction = transferTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No transaction found with id: [%s]", transactionId)));
        return transaction.getStatus();
    }
}
