package aq.project.aspect;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.messages.TransactionRequest;
import aq.project.messages.TransactionResponse;
import aq.project.metrics.ApplicationMeterRegistry;
import aq.project.services.TransactionService;
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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Matcher;

import static aq.project.util.RequestPropertyKeys.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class
TransactionServiceAspect {

    @Value("${spring.application.name}")
    private String tracerName;

    private final Validator validator;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMeterRegistry applicationMeterRegistry;

    @Timed(value = "transaction-service.send_transaction_request_time")
    @Around("execution(* aq.project.services.TransactionService.sendTransactionRequest(..)) && args(transactionRequest)")
    public String aspectSendTransactionRequest(ProceedingJoinPoint pjp, TransactionRequest transactionRequest) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("send_transaction_request").startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = Span.current().getSpanContext().getSpanId();
            String traceId = Span.current().getSpanContext().getTraceId();
//            Validation
            if(transactionRequest == null) {
                String msg = "Transaction request is null";
                ConstraintViolationException exc = new ConstraintViolationException(msg, null);
                log.warn(String.format("[%s-%s]: %s", spanId, traceId, msg));
                throw exc;
            }
            Set<ConstraintViolation<TransactionRequest>> constraintViolations = validator.validate(transactionRequest);
            if(!constraintViolations.isEmpty()) {
                ConstraintViolationException exc = new ConstraintViolationException(constraintViolations);
                log.warn(String.format("[%s-%s]: %s", spanId, traceId, exc.getMessage()));
                throw exc;
            }
            checkUuidProperties(transactionRequest, RECIPIENT_WALLET_ID, traceId, spanId);
            checkUuidProperties(transactionRequest, RECIPIENT_PERSON_ID, traceId, spanId);
            checkUuidProperties(transactionRequest, SENDER_WALLET_ID, traceId, spanId);
            checkUuidProperties(transactionRequest, SENDER_PERSON_ID, traceId, spanId);
            checkIsPositiveAmount(transactionRequest.getAmount(), traceId, spanId);
//            Telemetry
            String operation = transactionRequest.getOperationType().name().toLowerCase();
            log.info(String.format("[%s-%s]: Received %s transaction request with transaction id [%s]",
                    traceId, spanId, operation, transactionRequest.getTransactionId()));
//            Main logic
            String transactionId = (String) pjp.proceed(pjp.getArgs());
//            Telemetry
            log.info(String.format("[%s-%s]: Sending %s transaction request with transaction id [%s] completed successfully",
                    traceId, spanId, operation, transactionRequest.getTransactionId()));
            applicationMeterRegistry.incrementTotalSuccessTransactionRequestCounter();
            if(transactionRequest.getOperationType() == OperationType.DEPOSIT)
                applicationMeterRegistry.incrementSuccessDepositTransactionRequestCounter();
            else if(transactionRequest.getOperationType() == OperationType.WITHDRAW)
                applicationMeterRegistry.incrementSuccessWithdrawTransactionRequestCounter();
            else if(transactionRequest.getOperationType() == OperationType.TRANSFER)
                applicationMeterRegistry.incrementSuccessTransferTransactionRequestCounter();
            return transactionId;
        } finally {
            span.end();
        }
    }

    private void checkUuidProperties(TransactionRequest transactionRequest, String key, String traceId, String spanId) {
        if(!transactionRequest.isPropertyNull(key)) {
            String uuid = transactionRequest.getProperty(key);
            if(isInvalidUuid(uuid)) {
                applicationMeterRegistry.incrementTotalFailTransactionRequestCounter();
                String msg = String.format("Invalid %s UUID property found: %s", key, uuid);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
                throw new ConstraintViolationException(msg, null);
            }
        }
    }

    private boolean isInvalidUuid(String uuid) {
        String regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uuid);
        return !matcher.find();
    }

    private void checkIsPositiveAmount(BigDecimal amount, String traceId, String spanId) {
        if(amount.compareTo(new BigDecimal(0)) <= 0) {
            applicationMeterRegistry.incrementTotalFailTransactionRequestCounter();
            String msg = "The amount of the transaction must be greater than zero";
            log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
            throw new ConstraintViolationException(msg, null);
        }
    }

    @AfterThrowing(value = "execution(* aq.project.services.TransactionService.sendTransactionRequest(..)) && args(transactionRequest)", throwing = "e")
    public void aspectOnExceptionSendTransactionRequest(TransactionRequest transactionRequest, Exception e) throws Exception {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("send_transaction_request_exception").startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = Span.current().getSpanContext().getSpanId();
            String traceId = Span.current().getSpanContext().getTraceId();
            String operation = transactionRequest.getOperationType().name().toLowerCase();
            log.warn(String.format("[%s-%s]: Error occurred during sending %s transaction request with transaction id [%s]",
                    traceId, spanId, operation, transactionRequest.getTransactionId()), e);
            applicationMeterRegistry.incrementTotalFailTransactionRequestCounter();
            if(transactionRequest.getOperationType() == OperationType.DEPOSIT)
                applicationMeterRegistry.incrementFailDepositTransactionRequestCounter();
            else if(transactionRequest.getOperationType() == OperationType.WITHDRAW)
                applicationMeterRegistry.incrementFailWithdrawTransactionRequestCounter();
            else if(transactionRequest.getOperationType() == OperationType.TRANSFER)
                applicationMeterRegistry.incrementFailTransferTransactionRequestCounter();
            throw e;
        } finally {
            span.end();
        }
    }

    @Timed(value = "transaction-service.handle_transaction_response_time")
    @Around("execution(* aq.project.services.TransactionService.handleTransactionResponse(..)) && args(transactionResponse)")
    public void aspectHandleTransactionResponse(ProceedingJoinPoint pjp, TransactionResponse transactionResponse) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("handle_transaction_response").startSpan();
        String spanId = Span.current().getSpanContext().getSpanId();
        String traceId = Span.current().getSpanContext().getTraceId();
        try(Scope scope = span.makeCurrent()) {
//            Validation
            if(transactionResponse == null) {
                applicationMeterRegistry.incrementFailTransactionResponseCounter();
                String msg = "Transaction response is null";
                ConstraintViolationException exc = new ConstraintViolationException(msg, null);
                log.warn(String.format("[%s-%s]: %s", spanId, traceId, msg));
                throw exc;
            }
            Set<ConstraintViolation<TransactionResponse>> constraintViolations = validator.validate(transactionResponse);
            if(!constraintViolations.isEmpty()) {
                applicationMeterRegistry.incrementFailTransactionResponseCounter();
                ConstraintViolationException exc = new ConstraintViolationException(constraintViolations);
                log.warn(String.format("[%s-%s]: %s", spanId, traceId, exc.getMessage()));
                throw exc;
            }
//            Telemetry
            String operation = transactionResponse.getOperationType().name().toLowerCase();
            log.info(String.format("[%s-%s]: Received %s transaction response with id [%s]",
                    traceId, spanId, operation, transactionResponse.getTransactionId()));
//            Main logic
            int stausCode = (int) pjp.proceed(pjp.getArgs());
//            Telemetry
            if(HttpStatusCode.valueOf(stausCode).is2xxSuccessful()) {
                applicationMeterRegistry.incrementSuccessTransactionResponseCounter();
                log.info(String.format("[%s-%s]: Handle %s transaction response with id [%s] completed successfully",
                        traceId, spanId, operation, transactionResponse.getTransactionId()));
            } else if(HttpStatusCode.valueOf(stausCode).is4xxClientError()) {
                applicationMeterRegistry.incrementFailTransactionResponseCounter();
                log.warn(String.format("[%s-%s]: Handle %s transaction response with id [%s] failed. Check request parameters and try again",
                        traceId, spanId, operation, transactionResponse.getTransactionId()));
            } else if(HttpStatusCode.valueOf(stausCode).is5xxServerError()) {
                applicationMeterRegistry.incrementFailTransactionResponseCounter();
                log.warn(String.format("[%s-%s]: Handle %s transaction response with id [%s] failed. Unexpected individuals-api service exception occurred. Achieved max number of attempts (%d)",
                        traceId, spanId, operation, transactionResponse.getTransactionId(), TransactionService.MAX_NUMBER_OF_RETRIES));
            }
        } finally {
            span.end();
        }
    }

    @Timed(value = "transaction-service.get_transaction_status_time")
    @Around("execution(* aq.project.services.TransactionService.getTransactionStatus(..)) && args(transactionId)")
    public TransactionStatus aspectGetTransactionStatus(ProceedingJoinPoint pjp, String transactionId) throws Throwable {
        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder("get_transaction_status").startSpan();
        try(Scope scope = span.makeCurrent()) {
            String spanId = Span.current().getSpanContext().getSpanId();
            String traceId = Span.current().getSpanContext().getTraceId();
//            Validation
            if (transactionId == null || transactionId.isEmpty()) {
                String msg = "The transaction id is null or empty";
                ConstraintViolationException exc = new ConstraintViolationException(msg, null);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
                throw exc;
            }
            if (isInvalidUuid(transactionId)) {
                String msg = String.format("Received invalid transactionId: %s", transactionId);
                log.warn(String.format("[%s-%s]: %s", traceId, spanId, msg));
                throw new ConstraintViolationException(msg, null);
            }
//            Telemetry
            log.info(String.format("[%s-%s]: Received get transaction status request for transaction with id [%s]", traceId, spanId, transactionId));
//            Main logic
            TransactionStatus transactionStatus = (TransactionStatus) pjp.proceed(pjp.getArgs());
//            Telemetry
            log.info(String.format("[%s-%s]: The handle of request of getting transaction status with id [%s] completed successfully",
                    traceId, spanId, transactionId));
            applicationMeterRegistry.incrementSuccessGetTransactionStatusCounter();
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
