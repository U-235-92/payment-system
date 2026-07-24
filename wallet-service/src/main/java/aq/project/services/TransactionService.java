package aq.project.services;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Transaction;
import aq.project.exceptions.CreditCardConstrainsException;
import aq.project.exceptions.NoSuchWalletException;
import aq.project.exceptions.TransactionException;
import aq.project.exceptions.WalletConstrainsException;
import aq.project.messages.TransactionRequest;
import aq.project.messages.TransactionResponse;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.handlers.CommonRequestHandler;
import aq.project.utils.handlers.DepositRequestHandler;
import aq.project.utils.handlers.TransferRequestHandler;
import aq.project.utils.handlers.WithdrawRequestHandler;
import aq.project.utils.mappers.TransactionMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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

import java.util.List;
import java.util.concurrent.ExecutionException;

import static aq.project.dto.OperationType.*;
import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    @Value("${spring.application.name}")
    private String tracerName;

    @Value("${service.kafka.partitions.deposit.name}")
    private String depositPartitionName;

    @Value("${service.kafka.partitions.withdraw.name}")
    private String withdrawPartitionName;

    @Value("${service.kafka.partitions.transfer.name}")
    private String transferPartitionName;

    @Value("${service.kafka.topics.wallet_operation_response.name}")
    private String walletOperationResponseTopicName;

    private final DepositRequestHandler depositRequestHandler;
    private final WithdrawRequestHandler withdrawRequestHandler;
    private final TransferRequestHandler transferRequestHandler;

    private final OpenTelemetry openTelemetry;

    private final TransactionMapper transactionMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final TransactionRepository transactionRepository;

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.wallet_operation_request.name}")
    public void handleTransactionRequest(ConsumerRecord<String, TransactionRequest> consumerRecord) throws WalletConstrainsException, NoSuchWalletException, CreditCardConstrainsException {
        TransactionRequest transactionRequest = consumerRecord.value();
        switch(transactionRequest.getOperationType()) {
            case WITHDRAW -> handleTransactionRequest(transactionRequest, withdrawRequestHandler);
            case DEPOSIT -> handleTransactionRequest(transactionRequest, depositRequestHandler);
            case TRANSFER -> handleTransactionRequest(transactionRequest, transferRequestHandler);
        }
    }

    private void handleTransactionRequest(
            TransactionRequest transactionRequest,
            CommonRequestHandler commonRequestHandler
    ) throws WalletConstrainsException, CreditCardConstrainsException {
        commonRequestHandler.handleRequestMessage(transactionRequest, transactionMapper, transactionRepository);
    }

    public TransactionStatus getTransactionStatus(String transactionId) throws TransactionException {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(String.format("Transaction with id [%s] not found",
                        transactionId)));
        return transaction.getTransactionStatus();
    }

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void handleIncomingTransaction() {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("handle_incomming_transaction").startSpan();
        try(Scope scope = span.makeCurrent()) {
            Pageable pageable = PageRequest.of(0, 100, Sort.by("timestamp").descending());
            List<Transaction> transactionEvents = transactionRepository.findAll(pageable).getContent();
            for(Transaction transaction : transactionEvents) {
                if(!transaction.isProcessed()) {
                    switch (transaction.getOperationType()) {
                        case DEPOSIT -> handleIncomingTransaction(transaction, DEPOSIT, depositPartitionName, span);
                        case WITHDRAW -> handleIncomingTransaction(transaction, WITHDRAW, withdrawPartitionName, span);
                        case TRANSFER -> handleIncomingTransaction(transaction, TRANSFER, transferPartitionName, span);
                    }
                }
            }
        } finally {
            span.end();
        }
    }

    private void handleIncomingTransaction(Transaction transaction, OperationType operationType, String partition, Span span) {
        String spanId = span.getSpanContext().getSpanId();
        String traceId = transaction.getTraceId();
        try {
            log.info("[{}-{}]: Attempt to handle {} transaction event with transactionId [{}]",
                    traceId, spanId, operationType.name().toLowerCase(), transaction.getTransactionId());
            ProducerRecord<String, TransactionResponse> record = new ProducerRecord<>(
                    walletOperationResponseTopicName,
                    partition,
                    transactionMapper.toTransactionResponse(transaction));
            Headers headers = record.headers();
            headers.add(X_TRACE_ID_HEADER, traceId.getBytes());
            kafkaTemplate.send((ProducerRecord) record).get();
            transaction.setProcessed(true);
            log.info("[{}-{}]: Handle of {} transaction event with transactionId [{}] completed",
                    traceId, spanId, operationType.name(), transaction.getTransactionId());
        } catch (InterruptedException | ExecutionException exc) {
            log.warn("[{}-{}]: Handle of {} transaction event with transactionId [{}] failed",
                    traceId, spanId, operationType.name(), transaction.getTransactionId());
            throw new RuntimeException(exc);
        }
    }
}
