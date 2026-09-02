package aq.project.services.transaction;

import aq.project.dto.TransactionStatus;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.dto.WithdrawTransactionRequestDto;
import aq.project.dto.WithdrawTransactionResponseDto;
import aq.project.repositories.transaction.WithdrawTransactionRepository;
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
public class WithdrawTransactionService {

    @Value("${spring.application.name}")
    private String tracerName;

    @Value("${service.kafka.topics.withdraw_transaction_response.name}")
    private String transactionResponseTopicName;

    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;
    private final TransactionResponseMapper transactionResponseMapper = TransactionResponseMapper.INSTANCE;

    private final TransactionHandler transactionHandler;

    private final TraceContext traceContext;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OpenTelemetry openTelemetry;

    private final WithdrawTransactionRepository withdrawTransactionRepository;

    private final WalletService walletService;

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.withdraw_transaction_request.name}")
    public void handleTransactionRequest(WithdrawTransactionRequestDto request) {
        WithdrawTransaction transaction = transactionRequestMapper.toWithdrawTransaction(request);
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

    private void checkIdempotentTransactionHandle(WithdrawTransactionRequestDto request) {
        UUID transactionId = request.getTransactionId();
        transactionHandler.checkWithdrawTransactionPresent(transactionId);
    }

    private void checkTransactionConstraints(WithdrawTransactionRequestDto request) {
        UUID walletId = request.getWalletId();

        Wallet wallet = walletService.getWallet(walletId);
        CreditCard creditCard = wallet.getCreditCard();

        if(walletService.isWalletBlocked(wallet))
            throw new EntityConstraintsException(
                    String.format("Wallet with id: [%s] is blocked", walletId));

        if(walletService.isWalletCreditCardExpired(creditCard))
            throw new EntityConstraintsException(
                    String.format("Credit card with number: [%s] of wallet with id: [%s] is expired",
                            creditCard.getCardNumber(), walletId));

        if(walletService.isWalletCreditCardBalanceLessThan(creditCard, request.getAmount()))
            throw new EntityConstraintsException(
                    String.format("Credit card with number: [%s] has not enough money. Current balance: %s. Requested: %s",
                            creditCard.getCardNumber(),
                            creditCard.getBalance(),
                            request.getAmount()));
    }

    private void handleTransaction(WithdrawTransactionRequestDto request) {
//        Prepare data
        UUID walletId = request.getWalletId();
        Wallet wallet = walletService.getWalletWithLock(walletId);

//        Get conversion rate
        BigDecimal conversionRate = request.getConversionRate();

//        Execute operation
        BigDecimal currentBalance = wallet.getCreditCard().getBalance();
        BigDecimal convertedAmount = request.getAmount().multiply(conversionRate);
        BigDecimal updatedBalance = currentBalance.subtract(convertedAmount);

        wallet.getCreditCard().setBalance(updatedBalance);
    }

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void handleTransaction() {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("handle_withdraw_transaction").startSpan();
        try(Scope scope = span.makeCurrent()) {
            int pageNumber = 0, pageSize = 100;
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("metadata.timestamp").descending());
            List<WithdrawTransaction> withdrawTransactions = withdrawTransactionRepository.findAll(pageable).getContent();
            for(WithdrawTransaction transaction : withdrawTransactions) {
                try {
                    handleTransaction(transaction, span);
                } catch (Exception e) {
                    String traceId = transaction.getMetadata().getTraceId();
                    String spanId = span.getSpanContext().getSpanId();

                    log.warn("[{}-{}]: Error occurred while handle withdraw transaction with id: [{}]",
                            traceId, spanId, transaction.getId());
                }
            }
        } finally {
            span.end();
        }
    }

    private void handleTransaction(WithdrawTransaction transaction, Span span) throws Exception {
        String traceId = transaction.getMetadata().getTraceId();
        String spanId = span.getSpanContext().getSpanId();

        log.info("[{}-{}]: Attempt to handle withdraw transaction with id: [{}]",
                traceId, spanId, transaction.getId());

        WithdrawTransactionResponseDto response = transactionResponseMapper.toWithdrawResponse(transaction);

        ProducerRecord<String, WithdrawTransactionResponseDto> record = new ProducerRecord<>(transactionResponseTopicName, response);

        Headers headers = record.headers();
        headers.add(X_TRACE_ID_HEADER, traceId.getBytes());

        kafkaTemplate.send((ProducerRecord) record).get();

        transaction.setProcessed(true);

        log.info("[{}-{}]: Handle of withdraw transaction with id: [{}] completed",
                traceId, spanId, transaction.getId());
    }

    public TransactionStatus getTransactionStatus(UUID transactionId) {
        WithdrawTransaction transaction = withdrawTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No transaction found with id: [%s]", transactionId)));
        return transaction.getStatus();
    }
}
