package aq.project.utils.handlers;

import aq.project.dto.PaymentProviderServiceFailTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailTransactionHandler {

    @Value("${spring.application.name}")
    private String serviceName;

    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;

    private final Validator validator;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.fail_transaction_request.name}")
    public void handleFailTransaction(
            PaymentProviderServiceFailTransactionRequestDto request
    ) {
        if(request != null) {
            UUID transactionId = request.getTransactionId();

            String traceId = request.getTraceId();
            String action = "handle-fail-transaction-request";
            String tracerName = serviceName + "." + action + "-tracer";

            Tracer tracer = openTelemetry.getTracer(tracerName);
            Span span = tracer.spanBuilder(action).startSpan();

            String spanId = span.getSpanContext().getSpanId();
            String logMessageOnReceive = String.format(
                    "Received fail transaction request with transaction id: [%s]", transactionId);

            log.info("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnReceive);

            Timer.Sample sample = applicationMetricsRegistry.startTimer();

            try(Scope scope = span.makeCurrent()) {
                validateCreateTransactionRequest(request, traceId, spanId, action);

                String merchantId = request.getMerchantId();

                TransactionStatus targetStatus = TransactionStatus.MARKED_FAILED;

                Consumer<Transaction> handleFailTransaction = transaction -> {
                    transaction.setStatus(targetStatus);
                    transactionRepository.save(transaction);
                };

                handleOperationTransaction(
                        transactionId,
                        merchantId,
                        traceId,
                        spanId,
                        action,
                        targetStatus,
                        handleFailTransaction
                );
            } finally {
                applicationMetricsRegistry.finishTimer(sample, action);
                span.end();
            }
        }
    }

    private void validateCreateTransactionRequest(
            PaymentProviderServiceFailTransactionRequestDto request,
            String traceId,
            String spanId,
            String action
    ) {
        Set<ConstraintViolation<PaymentProviderServiceFailTransactionRequestDto>> violationSet = validator.validate(request);

        if(!violationSet.isEmpty()) {
            Function<ConstraintViolation<PaymentProviderServiceFailTransactionRequestDto>, String> violationToString = (violation) ->
                    String.format("%s = %s", violation.getPropertyPath().toString(), violation.getInvalidValue());

            String violations = violationSet
                    .stream()
                    .map(violationToString)
                    .reduce("", String::concat);

            String logOnInvalidDto = String.format(
                    "Handle of operation [%s] was interrupted. Received invalid fail transaction request: %s",
                    action, violations);

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logOnInvalidDto);

            applicationMetricsRegistry.countAction(false, action);

            throw new ConstraintViolationException(violationSet);
        }
    }

    private void handleOperationTransaction(
            UUID transactionId,
            String merchantId,
            String traceId,
            String spanId,
            String action,
            TransactionStatus targetStatus,
            Consumer<Transaction> consumer
    ) {
        String logMessageOnAttemptHandle = String.format(
                "Attempt to handle fail of created transaction request with transaction id: [%s]",
                transactionId);

        log.info("[{}-{}][{} -> {}]: {}",
                traceId, spanId, serviceName, action, logMessageOnAttemptHandle);

        Transaction transaction = getTransaction(transactionId);

        if(merchantRepository.existsById(merchantId)) {
            Merchant merchant = transaction.getMerchant();

            if(merchant.getId().equals(merchantId)) {

                switch(transaction.getStatus()) {

                    case PENDING, COMPLETED -> {
                        consumer.accept(transaction);

                        String logMessageOnSuccessHandle = String.format(
                                "Attempt to handle fail of created transaction request with transaction id: [%s] " +
                                "completed successfully",
                                transactionId);

                        log.info("[{}-{}][{} -> {}]: {}.",
                                traceId, spanId, serviceName, action, logMessageOnSuccessHandle);

                        applicationMetricsRegistry.countAction(true, action);
                    }
                    default -> {
                        TransactionStatus currentStatus = transaction.getStatus();

                        String logMessageOnErrorHandle = String.format(
                                "Attempt to handle fail of created transaction request with transaction id: [%s] completed with error. " +
                                "Prohibited attempt to change status of transaction from: [%s] to: [%s]",
                                transactionId, currentStatus, targetStatus);

                        log.error("[{}-{}][{} -> {}]: {}.",
                                traceId, spanId, serviceName, action, logMessageOnErrorHandle);

                        applicationMetricsRegistry.countAction(false, action);

                        throw new ProhibitedOperationException(
                                String.format("Prohibited attempt to change status of transaction with id: [%s] from: [%s] to: [%s]",
                                        transactionId, currentStatus, targetStatus.getValue()));
                    }
                }
            } else {
                String logMessageOnErrorHandle = String.format(
                        "Attempt to handle fail of created transaction request with transaction id: [%s] completed with error. " +
                        "Transaction belongs to another merchant",
                        transactionId);

                log.error("[{}-{}][{} -> {}]: {}.",
                        traceId, spanId, serviceName, action, logMessageOnErrorHandle);

                applicationMetricsRegistry.countAction(false, action);

                throw new ForeignMerchantTransactionException(
                        String.format("Transaction with id [%s] belongs to another merchant", transactionId));
            }
        } else {
            String logMessageOnErrorHandle = String.format(
                    "Attempt to handle fail of created transaction request with transaction id: [%s] completed with error. " +
                    "Merchant with id [%s] not found",
                    transactionId, merchantId);

            log.error("[{}-{}][{} -> {}]: {}.",
                    traceId, spanId, serviceName, action, logMessageOnErrorHandle);

            applicationMetricsRegistry.countAction(false, action);

            throw new EntityNotFoundException(
                    String.format("Merchant with id [%s] not found", merchantId));
        }
    }

    public Transaction getTransaction(
            UUID transactionId
    ) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id [%s] not found", transactionId)));
    }
}
