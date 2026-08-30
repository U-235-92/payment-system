package aq.project.utils.handlers;

import aq.project.dto.TransactionResponseDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantTransactionException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.repositories.MerchantRepository;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.mappers.TransactionMapper;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionHandler {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${service.kafka.topics.create_transaction_response.name}")
    private String createTransactionResponseTopic;
    @Value("${service.kafka.topics.fail_transaction_response.name}")
    private String failTransactionResponseTopic;
    @Value("${service.kafka.topics.cancel_transaction_response.name}")
    private String cancelTransactionResponseTopic;

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final OpenTelemetry openTelemetry;

    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Validator validator;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    @Transactional
    public void handleCreateTransaction(
            Transaction transaction,
            String merchantId
    ) {
        transaction.setStatus(TransactionStatus.PENDING);

        if(transactionRepository.existsById(transaction.getId()))
            throw new EntityAlreadyExistsException(
                    String.format("Transaction with id: [%s] already exists",
                            transaction.getId()));

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Merchant with id [%s] not found", merchantId)));

        transaction.setMerchant(merchant);

        transactionRepository.save(transaction);
    }

    @Transactional
    public void handleFailTransaction(
            UUID transactionId,
            String merchantId
    ) {
        TransactionStatus newStatus = TransactionStatus.MARKED_FAILED;

        Consumer<Transaction> handleFailTransaction = transaction -> {
            transaction.setStatus(newStatus);
            transactionRepository.save(transaction);
        };

        handleOperationTransaction(transactionId, merchantId, newStatus, handleFailTransaction);
    }

    @Transactional
    public void handleCancelTransaction(
            UUID transactionId,
            String merchantId
    ) {
        TransactionStatus newStatus = TransactionStatus.MARKED_CANCELED;

        Consumer<Transaction> handleCancelTransaction = transaction -> {
            transaction.setStatus(newStatus);
            transactionRepository.save(transaction);
        };

        handleOperationTransaction(transactionId, merchantId, newStatus, handleCancelTransaction);
    }

    private void handleOperationTransaction(
            UUID transactionId,
            String merchantId,
            TransactionStatus newStatus,
            Consumer<Transaction> consumer
    ) {
        Transaction transaction = getTransaction(transactionId);
        if(merchantRepository.existsById(merchantId)) {
            Merchant merchant = transaction.getMerchant();
            if(merchant.getId().equals(merchantId)) {
                switch(transaction.getStatus()) {
                    case PENDING, COMPLETED:
                        consumer.accept(transaction);
                    default:
                        String currentStatus = transaction.getStatus().getValue();
                        throw new ProhibitedOperationException(
                                String.format("Prohibited attempt to change status of transaction with id: [%s] from: [%s] to: [%s]",
                                        transactionId, currentStatus, newStatus.getValue()));
                }
            } else {
                throw new ForeignMerchantTransactionException(
                        String.format("Transaction with id [%s] belongs to another merchant", transactionId));
            }
        } else {
            throw new EntityNotFoundException(
                    String.format("Merchant with id [%s] not found", merchantId));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleScheduleCreateTransaction(
            Transaction transaction
    ) {
        String transactionId = transaction.getId().toString();
        String action = "handle-schedule-create-transaction";
        String logMessageOnReceive = String.format(
                "Start process operation: [%s] for transaction with id: [%s]",
                action, transactionId);
        String logMessageOnSuccess = String.format(
                "Finish process operation: [%s] for transaction with id: [%s]",
                action, transactionId);
        String logMessageOnError = String.format(
                "Error occurred during process operation: [%s] for transaction with id: [%s] while send response to topic [%s]",
                action, transactionId, createTransactionResponseTopic);

        handleScheduleOperationTransaction(
                transaction,
                TransactionStatus.COMPLETED,
                action,
                logMessageOnReceive,
                logMessageOnSuccess,
                logMessageOnError
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleScheduleFailTransaction(
            Transaction transaction
    ) {
        String transactionId = transaction.getId().toString();
        String action = "handle-schedule-fail-transaction";
        String logMessageOnReceive = String.format(
                "Start process operation: [%s] for transaction with id: [%s]",
                action, transactionId);
        String logMessageOnSuccess = String.format(
                "Finish process operation: [%s] for transaction with id: [%s]",
                action, transactionId);
        String logMessageOnError = String.format(
                "Error occurred during process operation: [%s] for transaction with id: [%s] while send response to topic [%s]",
                action, transactionId, failTransactionResponseTopic);

        if(transaction.getStatus() == TransactionStatus.MARKED_FAILED) {
            handleScheduleOperationTransaction(
                    transaction,
                    TransactionStatus.FAILED,
                    action,
                    logMessageOnReceive,
                    logMessageOnSuccess,
                    logMessageOnError
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleScheduleCancelTransaction(
            Transaction transaction
    ) {
        String transactionId = transaction.getId().toString();
        String action = "handle-schedule-cancel-transaction";
        String logMessageOnReceive = String.format(
                "Start process operation: [%s] for transaction with id: [%s]",
                action, transactionId);
        String logMessageOnSuccess = String.format(
                "Finish process operation: [%s] for transaction with id: [%s]",
                action, transactionId);
        String logMessageOnError = String.format(
                "Error occurred during process operation: [%s] for transaction with id: [%s] while send response to topic [%s]",
                action, transactionId, cancelTransactionResponseTopic);

        if(transaction.getStatus() == TransactionStatus.MARKED_CANCELED) {
            handleScheduleOperationTransaction(
                    transaction,
                    TransactionStatus.CANCELED,
                    action,
                    logMessageOnReceive,
                    logMessageOnSuccess,
                    logMessageOnError
            );
        }
    }

    private void handleScheduleOperationTransaction(
            Transaction transaction,
            TransactionStatus newStatus,
            String action,
            String logMessageOnReceive,
            String logMessageOnSuccess,
            String logMessageOnError
    ) {
        String traceId = transaction.getMetadata().getTraceId();
        String tracerName = serviceName + "." + action + "-tracer";

        UUID transactionId = transaction.getId();

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String spanId = span.getSpanContext().getSpanId();

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        if(isValidTransaction(transaction, traceId, spanId, action)) {

            Timer.Sample sample = applicationMetricsRegistry.startTimer();

            try(Scope scope = span.makeCurrent()) {

                TransactionResponseDto responseDto = transactionMapper.toTransactionResponseDto(transaction);

                kafkaTemplate.send(createTransactionResponseTopic, responseDto).get();

                transaction.setStatus(newStatus);

                log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnSuccess);

                applicationMetricsRegistry.countAction(true, action);
            } catch (ExecutionException | InterruptedException e) {
                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                applicationMetricsRegistry.countAction(false, action);

                String transactionErrorDescription = String.format(
                        "Error occurred during process operation: [%s] while send response to topic [%s]. " +
                        "Required manual intervention to resolve a problem. " +
                        "Logging info: transaction id: [%s], service name: [%s], action: [%s], trace id: [%s]",
                        action, createTransactionResponseTopic, transactionId, serviceName, action, traceId);

                transaction.setStatus(TransactionStatus.REQUIRED_MANUAL_HANDLE);
                transaction.setDescription(transactionErrorDescription);
            } finally {
                try {
                    transactionRepository.save(transaction);
                } catch (Exception e) {
                    String errorMessage = String.format(
                            "Unexpected error occurred while save transaction with id: [%s] during process: [%s]",
                            transactionId, action);
                    log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                            traceId, spanId, serviceName, action, errorMessage, e.getMessage());
                } finally {
                    applicationMetricsRegistry.finishTimer(sample, action);
                    span.end();
                }
            }
        } else {
            applicationMetricsRegistry.countAction(false, action);
            span.end();
        }
    }

    private boolean isValidTransaction(
            Transaction transaction,
            String traceId,
            String spanId,
            String action
    ) {
        Set<ConstraintViolation<Transaction>> constraintViolations = validator.validate(transaction);
        if(!constraintViolations.isEmpty()) {

            Function<ConstraintViolation<Transaction>, String> violationToString = (violation) ->
                    String.format("%s = %s", violation.getPropertyPath().toString(), violation.getInvalidValue());

            String violations = constraintViolations
                    .stream()
                    .map(violationToString)
                    .reduce("", String::concat);
            String errorMessage = String.format("Handle of operation [%s] was interrupted. Received invalid transaction: %s",
                    action, violations);

            log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, errorMessage);

            return false;
        }
        return true;
    }

    public Transaction getTransaction(
            UUID transactionId
    ) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Transaction with id [%s] not found", transactionId)));
    }
}
