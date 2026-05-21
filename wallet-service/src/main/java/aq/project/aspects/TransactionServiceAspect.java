package aq.project.aspects;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.OutboxEventException;
import aq.project.messages.TransactionRequest;
import aq.project.metrics.ApplicationMeterRegistry;
import aq.project.repositories.OutboxEventRepository;
import io.micrometer.core.annotation.Timed;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;

import static aq.project.util.RequestPropertyKeys.*;

@Slf4j
@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String tracerName;

    private final Validator validator;

    private final OpenTelemetry openTelemetry;

    private final OutboxEventRepository outboxEventRepository;

    private final ApplicationMeterRegistry applicationMeterRegistry;

    @Timed(value = "wallet-service.handle_transaction_request")
    @Around("execution(* aq.project.services.TransactionService.handleTransactionRequest(..)) && args(consumerRecord)")
    public void aspectHandleTransactionRequest(ProceedingJoinPoint pjp, ConsumerRecord<String, TransactionRequest> consumerRecord) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("handle_transaction_request").startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = Span.current().getSpanContext().getSpanId();
            String traceId = Span.current().getSpanContext().getTraceId();
            if(consumerRecord == null) {
                applicationMeterRegistry.incrementTotalFailRequestMessageCounter();
                log.warn(String.format("[%s-%s]: Received null Kafka consumer record", traceId, spanId));
                throw new ConstraintViolationException("Received null Kafka consumer record", null);
            }
            Headers headers = consumerRecord.headers();
            TransactionRequest transactionRequest = consumerRecord.value();
//            Validation
            if(transactionRequest == null) {
                applicationMeterRegistry.incrementTotalFailRequestMessageCounter();
                log.warn(String.format("[%s-%s]: Transaction request is null", traceId, spanId));
                throw new ConstraintViolationException("Transaction request is null", null);
            }
            Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(transactionRequest);
            if(!violations.isEmpty()) {
                applicationMeterRegistry.incrementTotalFailRequestMessageCounter();
                ConstraintViolationException exc = new ConstraintViolationException(violations);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, exc.getMessage()));
                throw exc;
            }
            checkUuidProperties(headers, RECIPIENT_WALLET_ID, traceId, spanId, transactionRequest.getTransactionId());
            checkUuidProperties(headers, RECIPIENT_PERSON_ID, traceId, spanId, transactionRequest.getTransactionId());
            checkUuidProperties(headers, SENDER_WALLET_ID, traceId, spanId, transactionRequest.getTransactionId());
            checkUuidProperties(headers, SENDER_PERSON_ID, traceId, spanId, transactionRequest.getTransactionId());
            checkIsPositiveAmount(transactionRequest.getAmount(), traceId, spanId);
//            Telemetry
            String operationType = transactionRequest.getOperationType().name().toLowerCase();
            log.info(String.format("[%s-%s]: Received %s message request with transactionId [%s]", traceId, spanId, operationType, transactionRequest.getTransactionId()));
            if(outboxEventRepository.findById(transactionRequest.getTransactionId()).isEmpty()) {
                log.info(String.format("[%s-%s]: Attempt to handle %s message request with transactionId [%s]", traceId, spanId, operationType, transactionRequest.getTransactionId()));
//                Main logic
                pjp.proceed(pjp.getArgs());
//                Telemetry
                log.info(String.format("[%s-%s]: Handle of %s message request with transactionId [%s] completed", traceId, spanId, operationType, transactionRequest.getTransactionId()));
                applicationMeterRegistry.incrementTotalSuccessRequestMessageCounter();
                if(transactionRequest.getOperationType() == OperationType.WITHDRAW)
                    applicationMeterRegistry.incrementSuccessWithdrawRequestMessageCounter();
                else if(transactionRequest.getOperationType() == OperationType.DEPOSIT)
                    applicationMeterRegistry.incrementSuccessDepositRequestMessageCounter();
                else if(transactionRequest.getOperationType() == OperationType.TRANSFER)
                    applicationMeterRegistry.incrementSuccessTransferRequestMessageCounter();
            } else {
                log.info(String.format("[%s-%s]: Attempt to handle duplicate of %s message request with transactionId [%s]", traceId, spanId, operationType, transactionRequest.getTransactionId()));
            }
        } finally {
            span.end();
        }
    }

    private void checkUuidProperties(Headers headers, String header, String traceId, String spanId, String transactionId) {
        String uuid = getPropertyFromHeader(headers, header);
        if(uuid != null) {
            if(isInvalidUuid(uuid)) {
                applicationMeterRegistry.incrementTotalFailRequestMessageCounter();
                String msg = String.format("Invalid %s UUID property found: %s in transaction request with transaction id [%s]",
                        header, uuid, transactionId);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
                throw new ConstraintViolationException(msg, null);
            }
        }
    }

    private String getPropertyFromHeader(Headers headers, String header) {
        try {
            byte[] bytes = headers.headers(header).iterator().next().value();
            return new String(bytes);
        } catch(NoSuchElementException e) {}
        return null;
    }

    private boolean isInvalidUuid(String uuid) {
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        return !matcher.find();
    }

    private void checkIsPositiveAmount(BigDecimal amount, String traceId, String spanId) {
        if(amount.compareTo(new BigDecimal(0)) <= 0) {
            applicationMeterRegistry.incrementTotalFailRequestMessageCounter();
            String msg = "The amount of the transaction must be greater than zero";
            log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
            throw new ConstraintViolationException(msg, null);
        }
    }

    @AfterThrowing(value = "execution(* aq.project.services.TransactionService.handleTransactionRequest(..)) && args(consumerRecord)", throwing = "e")
    public void aspectOnExceptionHandleTransactionRequest(ConsumerRecord<String, TransactionRequest> consumerRecord, Exception e) {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("handle_transaction_request").startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = Span.current().getSpanContext().getSpanId();
            String traceId = Span.current().getSpanContext().getTraceId();
            TransactionRequest transactionRequest = consumerRecord.value();
            String operationType = transactionRequest.getOperationType().name().toLowerCase();
            log.warn(String.format("[%s-%s]: Handle of %s message request with transactionId [%s] failed. %s",
                    traceId, spanId, operationType, transactionRequest.getTransactionId(), e.getMessage()));
            applicationMeterRegistry.incrementTotalFailRequestMessageCounter();
            if(transactionRequest.getOperationType() == OperationType.WITHDRAW)
                applicationMeterRegistry.incrementFailWithdrawRequestMessageCounter();
            else if(transactionRequest.getOperationType() == OperationType.DEPOSIT)
                applicationMeterRegistry.incrementFailDepositRequestMessageCounter();
            else if(transactionRequest.getOperationType() == OperationType.TRANSFER)
                applicationMeterRegistry.incrementFailTransferRequestMessageCounter();
        } finally {
            span.end();
        }
    }

    @Timed(value = "wallet-service.get_transaction_status_time")
    @Around("execution(* aq.project.services.TransactionService.getTransactionStatus(..)) && args(transactionId)")
    public TransactionStatus aspectGetTransactionStatus(ProceedingJoinPoint pjp, String transactionId) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("get_transaction_status").startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = Span.current().getSpanContext().getSpanId();
            String traceId = Span.current().getSpanContext().getTraceId();
//            Validation
            if(transactionId == null || transactionId.isEmpty()) {
                String msg = "The transaction id is null or empty";
                ConstraintViolationException exc = new ConstraintViolationException(msg, null);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
                throw exc;
            }
            if(isInvalidUuid(transactionId)) {
                String msg = String.format("Received invalid transactionId: %s", transactionId);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
                throw new ConstraintViolationException(msg, null);
            }
//            Telemetry
            log.info(String.format("[%s-%s]: Received get transaction status request for transaction with id [%s]", traceId, spanId, transactionId));
            if(outboxEventRepository.findById(transactionId).isEmpty()) {
                applicationMeterRegistry.incrementFailGetTransactionStatusCounter();
                String msg = String.format("Transaction with id [%s] not found", transactionId);
                OutboxEventException exc = new OutboxEventException(msg);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, exc.getMessage()));
                throw exc;
            }
//            Main logic
            TransactionStatus transactionStatus = (TransactionStatus) pjp.proceed();
//            Telemetry
            applicationMeterRegistry.incrementSuccessGetTransactionStatusCounter();
            log.info(String.format("[%s-%s]: The handle of request of getting transaction status with id [%s] completed successfully",
                    traceId, spanId, transactionId));
            return transactionStatus;
        } finally {
            span.end();
        }
    }

    @AfterThrowing(value = "execution(* aq.project.services.TransactionService.getTransactionStatus(..)) && args(transactionId)", throwing = "e")
    public void aspectOnExceptionGetTransactionStatus(String transactionId, Exception e) {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("get_transaction_status_exception").startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = Span.current().getSpanContext().getSpanId();
            String traceId = Span.current().getSpanContext().getTraceId();
            log.warn(String.format("[%s-%s]: %s", traceId, spanId, e.getMessage()), e);
            applicationMeterRegistry.incrementFailGetTransactionStatusCounter();
        } finally {
            span.end();
        }
    }
}
