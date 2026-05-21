package aq.project.services;

import aq.project.dto.OperationType;
import aq.project.entities.OutboxEvent;
import aq.project.mappers.OutboxEventMapper;
import aq.project.repositories.OutboxEventRepository;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {

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

    private final OpenTelemetry openTelemetry;

    private final OutboxEventMapper outboxEventMapper;

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void handleIncomingOutboxEvent() {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("handle_incomming_wallet_request").startSpan();
        try(Scope scope = span.makeCurrent()) {
            Pageable pageable = PageRequest.of(0, 100, Sort.by("timestamp").descending());
            List<OutboxEvent> transactionEvents = outboxEventRepository.findAll(pageable).getContent();
            for(OutboxEvent outboxEvent : transactionEvents) {
                if(outboxEvent.getOperationType() == OperationType.DEPOSIT && !outboxEvent.isProcessed())
                    handleIncomingOutboxEvent(outboxEvent, OperationType.DEPOSIT, depositPartitionName, span);
                else if(outboxEvent.getOperationType() == OperationType.WITHDRAW && !outboxEvent.isProcessed())
                    handleIncomingOutboxEvent(outboxEvent, OperationType.WITHDRAW, withdrawPartitionName, span);
                else if(outboxEvent.getOperationType() == OperationType.TRANSFER && !outboxEvent.isProcessed())
                    handleIncomingOutboxEvent(outboxEvent, OperationType.TRANSFER, transferPartitionName, span);
            }
        } finally {
            span.end();
        }
    }

    private void handleIncomingOutboxEvent(OutboxEvent outboxEvent, OperationType operationType, String partition, Span span) {
        String spanId = span.getSpanContext().getSpanId();
        String traceId = span.getSpanContext().getTraceId();
        try {
            log.info(String.format("[%s-%s]: Attempt to handle %s transaction event with transactionId [%s]", traceId, spanId, operationType.name().toLowerCase(), outboxEvent.getTransactionId()));
            kafkaTemplate.send(walletOperationResponseTopicName, partition, outboxEventMapper.toResponseMessage(outboxEvent)).get();
            outboxEvent.setProcessed(true);
            log.info(String.format("[%s-%s]: Handle of %s transaction event with transactionId [%s] completed", traceId, spanId, operationType.name(), outboxEvent.getTransactionId()));
        } catch (InterruptedException | ExecutionException exc) {
            log.warn(String.format("[%s-%s]: Handle of %s transaction event with transactionId [%s] failed", traceId, spanId, operationType.name(), outboxEvent.getTransactionId()));
            throw new RuntimeException(exc);
        }
    }
}
