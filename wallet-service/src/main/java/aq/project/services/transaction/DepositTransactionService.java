package aq.project.services.transaction;

import aq.project.dto.TransactionStatus;
import aq.project.dto.WalletServiceDepositTransactionRequestDto;
import aq.project.dto.WalletServiceDepositTransactionResponseDto;
import aq.project.dto.WalletServiceTransactionResponseErrorDto;
import aq.project.entities.transaction.DepositTransaction;
import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.repositories.transaction.DepositTransactionRepository;
import aq.project.services.wallet.WalletService;
import aq.project.utils.handlers.TransactionHandler;
import aq.project.utils.mappers.transaction.TransactionRequestMapper;
import aq.project.utils.mappers.transaction.TransactionResponseMapper;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import aq.project.utils.telemetry.TraceContext;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositTransactionService {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${service.kafka.topics.deposit_transaction_response.name}")
    private String transactionResponseTopicName;
    @Value("${service.kafka.topics.deposit_transaction_response_exceptions.name}")
    private String transactionExceptionResponseTopicName;

    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;
    private final TransactionResponseMapper transactionResponseMapper = TransactionResponseMapper.INSTANCE;

    private final TransactionHandler transactionHandler;

    private final TraceContext traceContext;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OpenTelemetry openTelemetry;

    private final DepositTransactionRepository depositTransactionRepository;

    private final WalletService walletService;

    private final Validator validator;

    @Transactional
    @KafkaListener(topics = "${service.kafka.topics.deposit_transaction_request.name}")
    public void handleTransactionRequest(
            WalletServiceDepositTransactionRequestDto request
    ) {
        if(request != null) {
            UUID transactionId = request.getTransactionId();

            String traceId = request.getTraceId();
            String action = "handle-deposit-transaction-request";
            String tracerName = serviceName + "." + action + "-tracer";

            Tracer tracer = openTelemetry.getTracer(tracerName);
            Span span = tracer.spanBuilder(action).startSpan();

            String spanId = span.getSpanContext().getSpanId();
            String logMessageOnReceive = String.format(
                    "Received request to handle deposit transaction with id: [%s]", transactionId);

            log.info("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnReceive);

            Timer.Sample sample = applicationMetricsRegistry.startTimer();

            try(Scope scope = span.makeCurrent()) {
                if(isValidRequestDto(request, traceId, spanId, action)) {
                    DepositTransaction transaction = transactionRequestMapper.toDepositTransaction(request);
                    try {
                        traceContext.clean();
                        traceContext.setTraceId(request.getTraceId());

                        checkIdempotentTransactionHandle(request);
                        checkTransactionConstraints(request);
                        handleTransaction(request);

                        transactionHandler.commitCompletedTransaction(transaction);

                        String logMessageOnSuccess = String.format(
                                "Request to handle deposit transaction with id: [%s] completed successfully",
                                transactionId);

                        log.info("[{}-{}][{} -> {}]: {}",
                                traceId, spanId, serviceName, action, logMessageOnSuccess);

                        applicationMetricsRegistry.countAction(true, action);
                    } catch(EntityNotFoundException | DuplicateTransactionHandleException | EntityConstraintsException e) {
                        String logMessageOnError = String.format(
                                "Error occurred while handle deposit transaction with id: [%s]. Exception: [%s]",
                                transactionId, e.getMessage());

                        log.error("[{}-{}][{} -> {}]: {}",
                                traceId, spanId, serviceName, action, logMessageOnError);

                        applicationMetricsRegistry.countAction(false, action);

                        transactionHandler.commitFailedTransaction(transaction);

                        WalletServiceTransactionResponseErrorDto errorResponseDto = transactionResponseMapper.toWalletServiceTransactionResponseErrorDto(request);
                        errorResponseDto.setTransactionStatus(TransactionStatus.FAILED);
                        errorResponseDto.setDescription(e.getMessage());

                        try {
                            String logMessageOnSendToKafka = String.format(
                                    "Attempt to send deposit transaction failure response with transaction id: [%s] " +
                                    "to kafka-topic: [%s]",
                                    transactionId, transactionExceptionResponseTopicName);

                            log.info("[{}-{}][{} -> {}]: {}",
                                    traceId, spanId, serviceName, action, logMessageOnSendToKafka);

                            kafkaTemplate.send(transactionExceptionResponseTopicName, errorResponseDto).get();

                            String logMessageOnCompletedSendToKafka = String.format(
                                    "Sending deposit transaction failure response with transaction id: [%s] " +
                                    "to kafka-topic: [%s] completed successfully",
                                    transactionId, transactionExceptionResponseTopicName);

                            log.info("[{}-{}][{} -> {}]: {}",
                                    traceId, spanId, serviceName, action, logMessageOnCompletedSendToKafka);
                        } catch(InterruptedException | ExecutionException ex) {
                            String logMessageOnFailedSendToKafka = String.format(
                                    "Exception occurred while send deposit transaction failure response with transaction id: [%s] " +
                                    "to kafka-topic: [%s]. Exception: [%s]",
                                    transactionId, transactionExceptionResponseTopicName, ex.getMessage());

                            log.error("[{}-{}][{} -> {}]: {}",
                                    traceId, spanId, serviceName, action, logMessageOnFailedSendToKafka);
                        }
                    }
                } else {
                    applicationMetricsRegistry.countAction(false, action);
                }
            } finally {
                applicationMetricsRegistry.finishTimer(sample, action);
                traceContext.clean();
                span.end();
            }
        }
    }

    private boolean isValidRequestDto(
            WalletServiceDepositTransactionRequestDto request,
            String traceId,
            String spanId,
            String action
    ) {
        Set<ConstraintViolation<WalletServiceDepositTransactionRequestDto>> violationSet = validator.validate(request);

        if(!violationSet.isEmpty()) {
            Function<ConstraintViolation<WalletServiceDepositTransactionRequestDto>, String> violationToString = (violation) ->
                    String.format("%s = %s", violation.getPropertyPath().toString(), violation.getInvalidValue());

            String violations = violationSet
                    .stream()
                    .map(violationToString)
                    .reduce("", String::concat);

            String logOnInvalidDto = String.format(
                    "Handle of operation [%s] was interrupted. Received invalid deposit transaction request: %s",
                    action, violations);

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logOnInvalidDto);

            return false;
        }
        return isValidAmount(request, traceId, spanId, action)
                & isValidConversionRate(request, traceId, spanId, action);
    }

    private boolean isValidAmount(
            WalletServiceDepositTransactionRequestDto request,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = request.getTransactionId();

        BigDecimal amount =  request.getAmount();

        if(amount.compareTo(new BigDecimal(0)) <= 0) {
            String logMessageOnError = String.format(
                    "Received deposit transaction request with transaction id: [%s] " +
                    "and with invalid amount value: [%s]",
                    transactionId, amount);

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);

            return false;
        }
        return true;
    }

    private boolean isValidConversionRate(
            WalletServiceDepositTransactionRequestDto request,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = request.getTransactionId();

        BigDecimal rate = request.getConversionRate();

        if(rate.compareTo(new BigDecimal(0)) <= 0) {
            String logMessageOnError = String.format(
                    "Received deposit transaction request with transaction id: [%s] " +
                    "and with invalid conversion rate value: [%s]",
                    transactionId, rate);

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);

            return false;
        }
        return true;
    }

    private void checkIdempotentTransactionHandle(
            WalletServiceDepositTransactionRequestDto request
    ) {
        UUID transactionId = request.getTransactionId();

        if(depositTransactionRepository.findById(transactionId).isPresent())
            throw new DuplicateTransactionHandleException(String
                    .format("Attempt to handle duplicate of deposit transaction with id: [%s]", transactionId));
    }

    private void checkTransactionConstraints(
            WalletServiceDepositTransactionRequestDto request
    ) {
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
    }

    private void handleTransaction(
            WalletServiceDepositTransactionRequestDto request
    ) {
        UUID walletId = request.getWalletId();
        Wallet wallet = walletService.getWalletWithLock(walletId);

        BigDecimal conversionRate = request.getConversionRate();

        BigDecimal currentBalance = wallet.getCreditCard().getBalance();
        BigDecimal convertedAmount = request.getAmount().multiply(conversionRate);
        BigDecimal updatedBalance = currentBalance.add(convertedAmount);

        wallet.getCreditCard().setBalance(updatedBalance);
    }

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void handleTransaction() {
        int pageNumber = 0, pageSize = 100;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("metadata.timestamp").descending());

        List<DepositTransaction> depositTransactions = depositTransactionRepository.findAll(pageable).getContent();

        for(DepositTransaction transaction : depositTransactions) {
            handleTransaction(transaction);
        }
    }

    private void handleTransaction(
            DepositTransaction transaction
    ) {
        UUID transactionId = transaction.getId();

        String action = "handle_deposit_transaction";
        String tracerName = serviceName + "." + action + "-tracer";

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String traceId = transaction.getMetadata().getTraceId();
        String spanId = span.getSpanContext().getSpanId();

        String logMessageOnReceive = String.format(
                "Attempt to handle deposit transaction with id: [%s]", transactionId);

        log.info("[{}-{}][{} -> {}]: {}",
                traceId, spanId, serviceName, action, logMessageOnReceive);

        Timer.Sample sample = applicationMetricsRegistry.startTimer();

        try(Scope scope = span.makeCurrent()) {
            WalletServiceDepositTransactionResponseDto depositTransactionResponseDto = transactionResponseMapper.toDepositResponse(transaction);

            try {
                kafkaTemplate.send(transactionResponseTopicName, depositTransactionResponseDto).get();

                String logMessageOnSuccess = String.format(
                        "Handle of deposit transaction with id: [%s] completed successfully", transactionId);

                log.info("[{}-{}][{} -> {}]: {}",
                        traceId, spanId, serviceName, action, logMessageOnSuccess);

                applicationMetricsRegistry.countAction(true, action);

                transaction.setProcessed(true);
            } catch (ExecutionException | InterruptedException e) {
                String logMessageOnFailedSendToKafka = String.format(
                        "Exception occurred while send deposit transaction handle response with transaction id: [%s] " +
                        "to kafka-topic: [%s]. Exception: [%s]",
                        transactionId, transactionResponseTopicName, e.getMessage());

                log.error("[{}-{}][{} -> {}]: {}",
                        traceId, spanId, serviceName, action, logMessageOnFailedSendToKafka);

                applicationMetricsRegistry.countAction(false, action);
            }
        } finally {
            applicationMetricsRegistry.finishTimer(sample, action);
            span.end();
        }
    }

    public TransactionStatus getTransactionStatus(
            UUID transactionId
    ) {
        DepositTransaction transaction = depositTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("No transaction found with id: [%s]", transactionId)));
        return transaction.getStatus();
    }
}
