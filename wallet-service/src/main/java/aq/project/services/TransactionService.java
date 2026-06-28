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
import aq.project.util.mappers.TransactionMapper;
import aq.project.util.handlers.DepositRequestHandler;
import aq.project.util.handlers.TransferRequestHandler;
import aq.project.util.handlers.WithdrawRequestHandler;
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
import static aq.project.util.constants.CustomHttpHeaders.*;
import static aq.project.util.constants.RequestPropertyKeys.RECIPIENT_WALLET_ID;
import static aq.project.util.constants.RequestPropertyKeys.SENDER_WALLET_ID;

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
        Headers headers = consumerRecord.headers();
        TransactionRequest transactionRequest = consumerRecord.value();
        switch(transactionRequest.getOperationType()) {
            case OperationType.WITHDRAW:
                transactionRequest.putProperty(RECIPIENT_WALLET_ID, getPropertyFromKafkaMessageHeader(headers, RECIPIENT_WALLET_ID));
                withdrawRequestHandler.handleRequestMessage(transactionRequest, transactionMapper, transactionRepository);
                break;
            case OperationType.DEPOSIT:
                transactionRequest.putProperty(RECIPIENT_WALLET_ID, getPropertyFromKafkaMessageHeader(headers, RECIPIENT_WALLET_ID));
                depositRequestHandler.handleRequestMessage(transactionRequest, transactionMapper, transactionRepository);
                break;
            case OperationType.TRANSFER:
                transactionRequest.putProperty(SENDER_WALLET_ID, getPropertyFromKafkaMessageHeader(headers, SENDER_WALLET_ID));
                transactionRequest.putProperty(RECIPIENT_WALLET_ID, getPropertyFromKafkaMessageHeader(headers, RECIPIENT_WALLET_ID));
                transferRequestHandler.handleRequestMessage(transactionRequest, transactionMapper, transactionRepository);
                break;
        }
    }

    private String getPropertyFromKafkaMessageHeader(Headers headers, String header) {
        byte[] bytes = headers.headers(header).iterator().next().value();
        return new String(bytes);
    }

    public TransactionStatus getTransactionStatus(String transactionId) throws TransactionException {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(String.format("Transaction with id [%s] not found", transactionId)));
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
                if(transaction.getOperationType() == OperationType.DEPOSIT && !transaction.isProcessed()) {
                    handleIncomingTransaction(transaction, OperationType.DEPOSIT, depositPartitionName, span);
                    continue;
                }
                if(transaction.getOperationType() == OperationType.WITHDRAW && !transaction.isProcessed()) {
                    handleIncomingTransaction(transaction, OperationType.WITHDRAW, withdrawPartitionName, span);
                    continue;
                }
                if(transaction.getOperationType() == OperationType.TRANSFER && !transaction.isProcessed()) {
                    handleIncomingTransaction(transaction, OperationType.TRANSFER, transferPartitionName, span);
                    continue;
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
            log.info("[{}-{}]: Attempt to handle {} transaction event with transactionId [{}]", traceId, spanId, operationType.name().toLowerCase(), transaction.getTransactionId());
            ProducerRecord<String, TransactionResponse> record = new ProducerRecord<>(
                    walletOperationResponseTopicName,
                    partition,
                    transactionMapper.toTransactionResponse(transaction));
            Headers headers = record.headers();
            headers.add(X_TRACE_ID_HEADER, traceId.getBytes());
            kafkaTemplate.send((ProducerRecord) record).get();
            transaction.setProcessed(true);
            log.info("[{}-{}]: Handle of {} transaction event with transactionId [{}] completed", traceId, spanId, operationType.name(), transaction.getTransactionId());
        } catch (InterruptedException | ExecutionException exc) {
            log.warn("[{}-{}]: Handle of {} transaction event with transactionId [{}] failed", traceId, spanId, operationType.name(), transaction.getTransactionId());
            throw new RuntimeException(exc);
        }
    }
}
