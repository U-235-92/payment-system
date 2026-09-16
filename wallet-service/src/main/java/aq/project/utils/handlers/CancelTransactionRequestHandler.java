package aq.project.utils.handlers;

import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceCancelDepositTransactionRequestDto;
import aq.project.dto.WalletServiceCancelTransferTransactionRequestDto;
import aq.project.dto.WalletServiceCancelWithdrawTransactionRequestDto;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.transaction.TransferTransaction;
import aq.project.entities.transaction.WithdrawTransaction;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.repositories.transaction.TransferTransactionRepository;
import aq.project.repositories.transaction.WithdrawTransactionRepository;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelTransactionRequestHandler {

    @Value("${spring.application.name}")
    private String serviceName;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    private final DepositTransactionRepository depositTransactionRepository;
    private final TransferTransactionRepository transferTransactionRepository;
    private final WithdrawTransactionRepository withdrawTransactionRepository;

    private final Validator validator;

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.cancel_deposit_transaction_request.name}")
    public void handleCancelDepositTransactionRequest(
            WalletServiceCancelDepositTransactionRequestDto request
    ) {
        if(request != null) {
            UUID transactionId = request.getTransactionId();

            OffsetDateTime requestTimestamp = request.getTimestamp();

            String traceId = request.getTraceId();
            String action = "handle-cancel-deposit-transaction-request";
            String tracerName = serviceName + "." + action + "-tracer";

            Tracer tracer = openTelemetry.getTracer(tracerName);
            Span span = tracer.spanBuilder(action).startSpan();

            String spanId = span.getSpanContext().getSpanId();
            String logMessageOnReceive = String.format(
                    "Received request to handle a cancel deposit transaction with id: [%s]", transactionId);

            log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnReceive);

            Timer.Sample sample = applicationMetricsRegistry.startTimer();

            try(Scope scope = span.makeCurrent()) {
                if(isValidRequestDto(request, traceId, spanId, action)) {
                    Optional<DepositTransaction> depositTransactionOptional = depositTransactionRepository.findById(transactionId);

                    if(depositTransactionOptional.isPresent()) {
                        DepositTransaction depositTransaction = depositTransactionOptional.get();

                        TransactionStatus currentDepositTransactionStatus = depositTransaction.getStatus();
                        TransactionStatus targetDepositTransactionStatus = TransactionStatus.CANCELED;

                        switch (currentDepositTransactionStatus) {
                            case PENDING, COMPLETED -> {
                                String description = String.format(
                                        "Status of transaction was changed from: [%s] to [%s]. " +
                                        "Required manual handle of current transaction. " +
                                        "Log info. Trace id: [%s], span id: [%s], action: [%s], request timestamp: [%s]",
                                        currentDepositTransactionStatus, targetDepositTransactionStatus, traceId, spanId, action, requestTimestamp);

                                depositTransaction.setStatus(targetDepositTransactionStatus);
                                depositTransaction.getMetadata().setDescription(description);
                                depositTransaction.getMetadata().setTimestamp(requestTimestamp);
                                depositTransaction.setProcessed(true);

                                depositTransactionRepository.save(depositTransaction);

                                String logMessageOnAction = String.format(
                                        "Status of transaction with id: [%s] was changed from: [%s] to [%s]. " +
                                        "Required manual handle of current transaction",
                                        transactionId, currentDepositTransactionStatus, targetDepositTransactionStatus);

                                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnAction);

                                applicationMetricsRegistry.countAction(true, action);
                            }
                            default -> {
                                String logMessageOnInvalidState = String.format(
                                        "Attempt to change transaction with id: [%s] from: [%s] to [%s] which is prohibited",
                                        transactionId, currentDepositTransactionStatus, targetDepositTransactionStatus);

                                log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnInvalidState);

                                applicationMetricsRegistry.countAction(false, action);
                            }
                        }
                    } else {
                        String logMessageOnException = String.format(
                                "Fail handle a cancel deposit transaction request with transaction id: [%s]. " +
                                "Transaction wasn't found",
                                transactionId);

                        log.error("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnException);

                        applicationMetricsRegistry.countAction(false, action);
                    }
                } else {
                    applicationMetricsRegistry.countAction(false, action);
                }
            } finally {
                applicationMetricsRegistry.finishTimer(sample, action);
                span.end();
            }
        }
    }

    private boolean isValidRequestDto(
            WalletServiceCancelDepositTransactionRequestDto request,
            String traceId,
            String spanId,
            String action
    ) {
        Set<ConstraintViolation<WalletServiceCancelDepositTransactionRequestDto>> violationSet = validator.validate(request);
        if(!violationSet.isEmpty()) {
            Function<ConstraintViolation<WalletServiceCancelDepositTransactionRequestDto>, String> violationToString = (violation) ->
                    String.format("%s = %s", violation.getPropertyPath().toString(), violation.getInvalidValue());

            String violations = violationSet
                    .stream()
                    .map(violationToString)
                    .reduce("", String::concat);

            String logOnInvalidDto = String.format(
                    "Handle of operation [%s] was interrupted. Received invalid deposit transaction request: %s",
                    action, violations);

            log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logOnInvalidDto);

            return false;
        }
        return true;
    }

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.cancel_withdraw_transaction_request.name}")
    public void handleCancelWithdrawTransactionRequest(
            WalletServiceCancelWithdrawTransactionRequestDto request
    ) {
        if(request != null) {
            UUID transactionId = request.getTransactionId();

            OffsetDateTime requestTimestamp = request.getTimestamp();

            String traceId = request.getTraceId();
            String action = "handle-cancel-withdraw-transaction-request";
            String tracerName = serviceName + "." + action + "-tracer";

            Tracer tracer = openTelemetry.getTracer(tracerName);
            Span span = tracer.spanBuilder(action).startSpan();

            String spanId = span.getSpanContext().getSpanId();
            String logMessageOnReceive = String.format(
                    "Received request to handle a cancel withdraw transaction with id: [%s]", transactionId);

            log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnReceive);

            Timer.Sample sample = applicationMetricsRegistry.startTimer();

            try(Scope scope = span.makeCurrent()) {
                if(isValidRequestDto(request, traceId, spanId, action)) {
                    Optional<WithdrawTransaction> withdrawTransactionOptional = withdrawTransactionRepository.findById(transactionId);

                    if(withdrawTransactionOptional.isPresent()) {
                        WithdrawTransaction withdrawTransaction = withdrawTransactionOptional.get();

                        TransactionStatus currentDepositTransactionStatus = withdrawTransaction.getStatus();
                        TransactionStatus targetDepositTransactionStatus = TransactionStatus.CANCELED;

                        switch (currentDepositTransactionStatus) {
                            case PENDING, COMPLETED -> {
                                String description = String.format(
                                        "Status of transaction was changed from: [%s] to [%s]. " +
                                        "Required manual handle of current transaction. " +
                                        "Log info. Trace id: [%s], span id: [%s], action: [%s], request timestamp: [%s]",
                                        currentDepositTransactionStatus, targetDepositTransactionStatus, traceId, spanId, action, requestTimestamp);

                                withdrawTransaction.setStatus(targetDepositTransactionStatus);
                                withdrawTransaction.getMetadata().setDescription(description);
                                withdrawTransaction.getMetadata().setTimestamp(requestTimestamp);
                                withdrawTransaction.setProcessed(true);

                                withdrawTransactionRepository.save(withdrawTransaction);

                                String logMessageOnAction = String.format(
                                        "Status of transaction with id: [%s] was changed from: [%s] to [%s]. " +
                                        "Required manual handle of current transaction",
                                        transactionId, currentDepositTransactionStatus, targetDepositTransactionStatus);

                                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnAction);

                                applicationMetricsRegistry.countAction(true, action);
                            }
                            default -> {
                                String logMessageOnInvalidState = String.format(
                                        "Attempt to change transaction with id: [%s] from: [%s] to [%s] which is prohibited",
                                        transactionId, currentDepositTransactionStatus, targetDepositTransactionStatus);

                                log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnInvalidState);

                                applicationMetricsRegistry.countAction(false, action);
                            }
                        }
                    } else {
                        String logMessageOnException = String.format(
                                "Fail handle a cancel withdraw transaction request with transaction id: [%s]. " +
                                "Transaction wasn't found",
                                transactionId);

                        log.error("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnException);

                        applicationMetricsRegistry.countAction(false, action);
                    }
                } else {
                    applicationMetricsRegistry.countAction(false, action);
                }
            } finally {
                applicationMetricsRegistry.finishTimer(sample, action);
                span.end();
            }
        }
    }

    private boolean isValidRequestDto(
            WalletServiceCancelWithdrawTransactionRequestDto request,
            String traceId,
            String spanId,
            String action
    ) {
        Set<ConstraintViolation<WalletServiceCancelWithdrawTransactionRequestDto>> violationSet = validator.validate(request);
        if(!violationSet.isEmpty()) {
            Function<ConstraintViolation<WalletServiceCancelWithdrawTransactionRequestDto>, String> violationToString = (violation) ->
                    String.format("%s = %s", violation.getPropertyPath().toString(), violation.getInvalidValue());

            String violations = violationSet
                    .stream()
                    .map(violationToString)
                    .reduce("", String::concat);

            String logOnInvalidDto = String.format(
                    "Handle of operation [%s] was interrupted. Received invalid withdraw transaction request: %s",
                    action, violations);

            log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logOnInvalidDto);

            return false;
        }
        return true;
    }

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.cancel_transfer_transaction_request.name}")
    public void handleCancelTransferTransactionRequest(
            WalletServiceCancelTransferTransactionRequestDto request
    ) {
        if(request != null) {
            UUID transactionId = request.getTransactionId();

            OffsetDateTime requestTimestamp = request.getTimestamp();

            String traceId = request.getTraceId();
            String action = "handle-cancel-transfer-transaction-request";
            String tracerName = serviceName + "." + action + "-tracer";

            Tracer tracer = openTelemetry.getTracer(tracerName);
            Span span = tracer.spanBuilder(action).startSpan();

            String spanId = span.getSpanContext().getSpanId();
            String logMessageOnReceive = String.format(
                    "Received request to handle a cancel transfer transaction with id: [%s]", transactionId);

            log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnReceive);

            Timer.Sample sample = applicationMetricsRegistry.startTimer();

            try(Scope scope = span.makeCurrent()) {
                if(isValidRequestDto(request, traceId, spanId, action)) {
                    Optional<TransferTransaction> transferTransactionOptional = transferTransactionRepository.findById(transactionId);

                    if(transferTransactionOptional.isPresent()) {
                        TransferTransaction transferTransaction = transferTransactionOptional.get();

                        TransactionStatus currentDepositTransactionStatus = transferTransaction.getStatus();
                        TransactionStatus targetDepositTransactionStatus = TransactionStatus.CANCELED;

                        switch (currentDepositTransactionStatus) {
                            case PENDING, COMPLETED -> {
                                String description = String.format(
                                        "Status of transaction was changed from: [%s] to [%s]. " +
                                        "Required manual handle of current transaction. " +
                                        "Log info. Trace id: [%s], span id: [%s], action: [%s], request timestamp: [%s]",
                                        currentDepositTransactionStatus, targetDepositTransactionStatus, traceId, spanId, action, requestTimestamp);

                                transferTransaction.setStatus(targetDepositTransactionStatus);
                                transferTransaction.getMetadata().setDescription(description);
                                transferTransaction.getMetadata().setTimestamp(requestTimestamp);
                                transferTransaction.setProcessed(true);

                                transferTransactionRepository.save(transferTransaction);

                                String logMessageOnAction = String.format(
                                        "Status of transaction with id: [%s] was changed from: [%s] to [%s]. " +
                                        "Required manual handle of current transaction",
                                        transactionId, currentDepositTransactionStatus, targetDepositTransactionStatus);

                                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnAction);

                                applicationMetricsRegistry.countAction(true, action);
                            }
                            default -> {
                                String logMessageOnInvalidState = String.format(
                                        "Attempt to change transaction with id: [%s] from: [%s] to [%s] which is prohibited",
                                        transactionId, currentDepositTransactionStatus, targetDepositTransactionStatus);

                                log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnInvalidState);

                                applicationMetricsRegistry.countAction(false, action);
                            }
                        }
                    } else {
                        String logMessageOnException = String.format(
                                "Fail handle a cancel transfer transaction request with transaction id: [%s]. " +
                                "Transaction wasn't found",
                                transactionId);

                        log.error("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnException);

                        applicationMetricsRegistry.countAction(false, action);
                    }
                } else {
                    applicationMetricsRegistry.countAction(false, action);
                }
            } finally {
                applicationMetricsRegistry.finishTimer(sample, action);
                span.end();
            }
        }
    }

    private boolean isValidRequestDto(
            WalletServiceCancelTransferTransactionRequestDto request,
            String traceId,
            String spanId,
            String action
    ) {
        Set<ConstraintViolation<WalletServiceCancelTransferTransactionRequestDto>> violationSet = validator.validate(request);
        if(!violationSet.isEmpty()) {
            Function<ConstraintViolation<WalletServiceCancelTransferTransactionRequestDto>, String> violationToString = (violation) ->
                    String.format("%s = %s", violation.getPropertyPath().toString(), violation.getInvalidValue());

            String violations = violationSet
                    .stream()
                    .map(violationToString)
                    .reduce("", String::concat);

            String logOnInvalidDto = String.format(
                    "Handle of operation [%s] was interrupted. Received invalid transfer transaction request: %s",
                    action, violations);

            log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logOnInvalidDto);

            return false;
        }
        return true;
    }
}
