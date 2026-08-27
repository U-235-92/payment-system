package aq.project.services;

import aq.project.dto.TransactionStatus;
import aq.project.entities.CreditCard;
import aq.project.entities.DepositTransaction;
import aq.project.entities.Wallet;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.messages.requests.DepositTransactionRequest;
import aq.project.messages.responses.DepositTransactionResponse;
import aq.project.repositories.DepositTransactionRepository;
import aq.project.utils.handlers.TransactionHandler;
import aq.project.utils.mappers.TransactionRequestMapper;
import aq.project.utils.mappers.TransactionResponseMapper;
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
import java.time.Instant;
import java.util.List;

import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositTransactionService {

    @Value("${spring.application.name}")
    private String tracerName;

    @Value("${service.kafka.topics.deposit_transaction_response.name}")
    private String transactionResponseTopicName;

    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;
    private final TransactionResponseMapper transactionResponseMapper = TransactionResponseMapper.INSTANCE;

    private final TransactionHandler transactionHandler;

    private final TraceContext traceContext;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OpenTelemetry openTelemetry;

    private final DepositTransactionRepository depositTransactionRepository;

    private final WalletService walletService;

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.deposit_transaction_request.name}")
    public void handleTransactionRequest(DepositTransactionRequest request) {
        DepositTransaction transaction = transactionRequestMapper.toDepositTransaction(request);
        try {
            traceContext.clean();
            traceContext.setTraceId(request.getTraceId());

            checkIdempotentTransactionHandle(request);
            checkTransactionConstraints(request);
            handleTransaction(request);

            transactionHandler.commitCompletedTransaction(transaction);
        } catch(EntityNotFoundException e) {
            transactionHandler.commitFailedTransaction(transaction);
            throw e;
        } finally {
            traceContext.clean();
        }
    }

    private void checkIdempotentTransactionHandle(DepositTransactionRequest request) {
        String transactionId = request.getTransactionId();
        transactionHandler.checkDepositTransactionPresent(transactionId);
    }

    private void checkTransactionConstraints(DepositTransactionRequest request) {
        String walletId = request.getWalletId();

        Wallet wallet = walletService.getWallet(walletId);
        CreditCard creditCard = wallet.getCreditCard();

        if(walletService.isWalletBlocked(wallet))
            throw new EntityConstraintsException(
                    String.format("Wallet with id: [%s] is blocked", walletId));

        if(walletService.isWalletCreditCardExpired(creditCard))
            throw new EntityConstraintsException(
                    String.format("Credit card with number: [%s] of wallet with id: [%s] is expired",
                            creditCard.getCardNumber(), walletId));
    }

    private void handleTransaction(DepositTransactionRequest request) {
        String walletId = request.getWalletId();
        Wallet wallet = walletService.getWalletWithLock(walletId);

        BigDecimal conversionRate = request.getConversionRate();

        BigDecimal currentBalance = wallet.getCreditCard().getBalance();
        BigDecimal convertedAmount = request.getAmount().multiply(conversionRate);
        BigDecimal updatedBalance = currentBalance.add(convertedAmount);

        wallet.getCreditCard().setBalance(updatedBalance);
        wallet.getCreditCard().getInstantEmbeddedData().setUpdated(Instant.now());
    }

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void handleTransaction() {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("handle_deposit_transaction").startSpan();
        try(Scope scope = span.makeCurrent()) {
            int pageNumber = 0, pageSize = 100;
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("metadata.timestamp").descending());
            List<DepositTransaction> depositTransactions = depositTransactionRepository.findAll(pageable).getContent();
            for(DepositTransaction transaction : depositTransactions) {
                try {
                    handleTransaction(transaction, span);
                } catch (Exception e) {
                    String traceId = transaction.getMetadata().getTraceId();
                    String spanId = span.getSpanContext().getSpanId();

                    log.warn("[{}-{}]: Error occurred while handle deposit transaction with id: [{}]",
                            traceId, spanId, transaction.getId());
                }
            }
        } finally {
            span.end();
        }
    }

    private void handleTransaction(DepositTransaction transaction, Span span) throws Exception {
        String traceId = transaction.getMetadata().getTraceId();
        String spanId = span.getSpanContext().getSpanId();

        log.info("[{}-{}]: Attempt to handle deposit transaction with id: [{}]",
                traceId, spanId, transaction.getId());

        DepositTransactionResponse response = transactionResponseMapper.toDepositResponse(transaction);

        ProducerRecord<String, DepositTransactionResponse> record = new ProducerRecord<>(transactionResponseTopicName, response);

        Headers headers = record.headers();
        headers.add(X_TRACE_ID_HEADER, traceId.getBytes());

        kafkaTemplate.send((ProducerRecord) record).get();

        transaction.setProcessed(true);

        log.info("[{}-{}]: Handle of deposit transaction with id: [{}] completed",
                traceId, spanId, transaction.getId());
    }

    public TransactionStatus getTransactionStatus(String transactionId) {
        DepositTransaction transaction = depositTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No transaction found with id: [%s]", transactionId)));
        return transaction.getStatus();
    }
}
