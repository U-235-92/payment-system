package aq.project.utils.handlers;

import aq.project.dto.PaymentProviderServiceCreateTransactionRequestDto;
import aq.project.dto.PaymentProviderServiceErrorHandleTransactionDto;
import aq.project.dto.PaymentProviderServiceSuccessHandleTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.Merchant;
import aq.project.entities.Transaction;
import aq.project.exceptions.DtoConstraintsException;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
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
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTransactionHandler {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${service.kafka.topics.create_transaction_response.name}")
    private String createTransactionResponseTopic;
    @Value("${service.kafka.topics.create_transaction_response_exceptions.name}")
    private String createTransactionResponseExceptionsTopic;

    private final TransactionMapper transactionMapper = TransactionMapper.INSTANCE;

    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;

    private final Validator validator;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.create_transaction_request.name}")
    public void handleCreateTransactionRequest(
            PaymentProviderServiceCreateTransactionRequestDto request
    ) {
        if(request != null) {
            UUID transactionId = request.getTransactionId();

            String traceId = request.getTraceId();
            String action = "handle-create-transaction-request";
            String tracerName = serviceName + "." + action + "-tracer";

            Tracer tracer = openTelemetry.getTracer(tracerName);
            Span span = tracer.spanBuilder(action).startSpan();

            String spanId = span.getSpanContext().getSpanId();
            String logMessageOnReceive = String.format(
                    "Received request to handle create transaction with id: [%s]", transactionId);

            log.info("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnReceive);

            Timer.Sample sample = applicationMetricsRegistry.startTimer();

            try(Scope scope = span.makeCurrent()) {
                validateCreateTransactionRequest(request, traceId, spanId, action);

                Transaction transaction = transactionMapper.toTransaction(request);

                if(transactionRepository.existsById(transaction.getId())) {
                    String logMessageOnTransactionAlreadyExists = String.format(
                            "Fail attempt to handle create transaction request with transaction id: [%s]. " +
                            "Transaction already exists",
                            transaction.getId());

                    log.warn("[{}-{}][{} -> {}]: {}.",
                            traceId, spanId, serviceName, action, logMessageOnTransactionAlreadyExists);

                    applicationMetricsRegistry.countAction(false, action);

                    String description = String.format(
                            "Transaction with id: [%s] already exists", transaction.getId());

                    PaymentProviderServiceErrorHandleTransactionDto errorResponseDto = transactionMapper.toPaymentProviderServiceErrorHandleTransactionDto(request);
                    errorResponseDto.setDescription(description);

                    sendWalletServiceTransactionErrorResponseDtoToKafka(errorResponseDto, traceId, spanId, action);

                    throw new EntityAlreadyExistsException(description);
                }

                String merchantId = request.getMerchantId();

                Optional<Merchant> merchantOptional = merchantRepository.findById(merchantId);

                if(merchantOptional.isPresent()) {
                    Merchant merchant = merchantOptional.get();

                    transaction.setStatus(TransactionStatus.PENDING);
                    transaction.setMerchant(merchant);

                    transactionRepository.save(transaction);

                } else {
                    String logMessageOnTransactionAlreadyExists = String.format(
                            "Fail attempt to handle create transaction request with transaction id: [%s]. " +
                            "Merchant with id [%s] not found",
                            transaction.getId(), merchantId);

                    log.warn("[{}-{}][{} -> {}]: {}.",
                            traceId, spanId, serviceName, action, logMessageOnTransactionAlreadyExists);

                    applicationMetricsRegistry.countAction(false, action);

                    String description = String.format("Merchant with id [%s] not found", merchantId);

                    PaymentProviderServiceErrorHandleTransactionDto errorResponseDto = transactionMapper.toPaymentProviderServiceErrorHandleTransactionDto(request);
                    errorResponseDto.setDescription(description);

                    sendWalletServiceTransactionErrorResponseDtoToKafka(errorResponseDto, traceId, spanId, action);

                    throw new EntityNotFoundException(description);
                }
            } finally {
                applicationMetricsRegistry.finishTimer(sample, action);
                span.end();
            }
        }
    }

    private void validateCreateTransactionRequest(
            PaymentProviderServiceCreateTransactionRequestDto request,
            String traceId,
            String spanId,
            String action
    ) {
        Set<ConstraintViolation<PaymentProviderServiceCreateTransactionRequestDto>> violationSet = validator.validate(request);

        if(!violationSet.isEmpty()) {
            Function<ConstraintViolation<PaymentProviderServiceCreateTransactionRequestDto>, String> violationToString = (violation) ->
                    String.format("%s = %s", violation.getPropertyPath().toString(), violation.getInvalidValue());

            String violations = violationSet
                    .stream()
                    .map(violationToString)
                    .reduce("", String::concat);

            String logOnInvalidDto = String.format(
                    "Handle of operation [%s] was interrupted. Received invalid create transaction request: %s",
                    action, violations);

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logOnInvalidDto);

            applicationMetricsRegistry.countAction(false, action);

            throw new ConstraintViolationException(violationSet);
        }

        boolean isValidAmount = isValidAmount(request, traceId, spanId, action);

        if(!isValidAmount) {
            applicationMetricsRegistry.countAction(false, action);

            String description = String.format("Request contains invalid parameter: amount = [%s]",
                    request.getAmount());

            PaymentProviderServiceErrorHandleTransactionDto errorResponseDto = transactionMapper.toPaymentProviderServiceErrorHandleTransactionDto(request);
            errorResponseDto.setDescription(description);

            sendWalletServiceTransactionErrorResponseDtoToKafka(errorResponseDto, traceId, spanId, description);

            throw new DtoConstraintsException(description);
        }
    }

    private boolean isValidAmount(
            PaymentProviderServiceCreateTransactionRequestDto request,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = request.getTransactionId();

        BigDecimal amount = request.getAmount();

        if(amount.compareTo(new BigDecimal(0)) <= 0) {
            String logMessageOnError = String.format(
                    "Received request to create transaction with transaction id: [%s] " +
                    "and with invalid amount value: [%s]",
                    transactionId, amount);

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);

            return false;
        }
        return true;
    }

    private void sendWalletServiceTransactionErrorResponseDtoToKafka(
            PaymentProviderServiceErrorHandleTransactionDto errorResponseDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = errorResponseDto.getTransactionId();

        try {
            String logMessageOnSendToKafka = String.format(
                    "Attempt to send failure create transaction response with transaction id: [%s] " +
                    "to kafka-topic: [%s]",
                    transactionId, createTransactionResponseExceptionsTopic);

            log.info("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnSendToKafka);

            kafkaTemplate.send(createTransactionResponseExceptionsTopic, errorResponseDto).get();

            String logMessageOnCompletedSendToKafka = String.format(
                    "Sending failure create transaction response with transaction id: [%s] " +
                    "to kafka-topic: [%s] completed successfully",
                    transactionId, createTransactionResponseExceptionsTopic);

            log.info("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnCompletedSendToKafka);

        } catch(InterruptedException | ExecutionException e) {
            String logMessageOnFailedSendToKafka = String.format(
                    "Exception occurred while send failure create transaction response with transaction id: [%s] " +
                    "to kafka-topic: [%s]. Exception: [%s]",
                    transactionId, createTransactionResponseExceptionsTopic, e.getMessage());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnFailedSendToKafka);
        }
    }

    @Transactional
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
                "Error occurred during process operation: [%s] for transaction with id: [%s] " +
                "while send response to topic [%s]",
                action, transactionId, createTransactionResponseTopic);

        handleScheduleOperationTransaction(
                transaction,
                action,
                logMessageOnReceive,
                logMessageOnSuccess,
                logMessageOnError
        );
    }

    private void handleScheduleOperationTransaction(
            Transaction transaction,
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

        Timer.Sample sample = applicationMetricsRegistry.startTimer();

        try(Scope scope = span.makeCurrent()) {
            PaymentProviderServiceSuccessHandleTransactionDto successResponseDto = transactionMapper.toPaymentProviderServiceSuccessHandleTransactionDto(transaction);

            kafkaTemplate.send(createTransactionResponseTopic, successResponseDto).get();

            transaction.setStatus(TransactionStatus.COMPLETED);

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
    }
}
