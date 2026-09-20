package aq.project.utils.handlers.payment_provider_service.response;

import aq.project.dto.Operation;
import aq.project.dto.PaymentProviderServiceErrorHandleTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.utils.telemetry.ApplicationMetricsRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class PaymentProviderServiceFailResponseCreateTransactionHandler {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${service.payment-provider-service.name}")
    private String paymentProviderServiceName;

    private final TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    private final TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    private final TransactionServiceTransferTransactionRepository transactionServiceTransferTransactionRepository;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    @KafkaListener(topics = "payment_provider_service_create_transaction_response_exceptions")
    public void handleFailResponseCreateTransactionOnPaymentProviderService(
            @NotNull @Valid PaymentProviderServiceErrorHandleTransactionDto paymentProviderServiceErrorHandleTransactionDto
    ) {
        UUID transactionId = paymentProviderServiceErrorHandleTransactionDto.getTransactionId();

        String traceId = paymentProviderServiceErrorHandleTransactionDto.getTraceId();
        String action = "handle-fail-response-create-transaction-on-payment-provider-service";
        String tracerName = serviceName + "." + action + "-tracer";

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String spanId = span.getSpanContext().getSpanId();
        String logMessageOnReceive = String.format("Received fail response from service: [%s] during process transaction with id: [%s]",
                paymentProviderServiceName, transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        Timer.Sample sample = applicationMetricsRegistry.startTimer();

        try(Scope scope = span.makeCurrent()) {
            Operation transactionOperation = paymentProviderServiceErrorHandleTransactionDto.getOperation();
            switch (transactionOperation) {
                case DEPOSIT -> handleTransactionServiceDepositTransaction(
                        paymentProviderServiceErrorHandleTransactionDto,
                        traceId,
                        spanId,
                        action
                );
                case WITHDRAW -> handleTransactionServiceWithdrawTransaction(
                        paymentProviderServiceErrorHandleTransactionDto,
                        traceId,
                        spanId,
                        action
                );
                case TRANSFER -> handleTransactionServiceTransferTransaction(
                        paymentProviderServiceErrorHandleTransactionDto,
                        traceId,
                        spanId,
                        action
                );
            }
        } finally {
//          TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
            applicationMetricsRegistry.finishTimer(sample, action);
            span.end();
        }
    }

//    -------------------------------------------------------------------------------------------------
//    -------------------------------------- DEPOSIT TRANSACTION --------------------------------------
//    -------------------------------------------------------------------------------------------------
    private void handleTransactionServiceDepositTransaction(
            PaymentProviderServiceErrorHandleTransactionDto paymentProviderServiceErrorHandleTransactionDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = paymentProviderServiceErrorHandleTransactionDto.getTransactionId();

        Optional<TransactionServiceDepositTransaction> transactionServiceDepositTransactionOptional = transactionServiceDepositTransactionRepository.findById(transactionId);

        if(transactionServiceDepositTransactionOptional.isPresent()) {
            handleFoundTransactionServiceDepositTransaction(
                    transactionServiceDepositTransactionOptional.get(),
                    traceId,
                    spanId,
                    action
            );
        } else {
            handleNotFoundTransactionServiceTransactionEntity(
                    paymentProviderServiceErrorHandleTransactionDto,
                    traceId,
                    spanId,
                    action
            );
        }
    }

    private void handleFoundTransactionServiceDepositTransaction(
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        if(transactionServiceDepositTransaction.getStatus() == TransactionStatus.PENDING) {
            handlePendingTransactionServiceDepositTransaction(
                    transactionServiceDepositTransaction,
                    traceId,
                    spanId,
                    action
            );
        } else {
            handleNotPendingTransactionServiceDepositTransaction(
                    transactionServiceDepositTransaction,
                    traceId,
                    spanId,
                    action
            );
        }
    }

    private void handlePendingTransactionServiceDepositTransaction(
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceDepositTransaction.getId();

        transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

        transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

        applicationMetricsRegistry.countAction(true, action);

        String logMessageOnSuccess = String.format(
                "Success handle of fail response from service: [%s] of deposit transaction with id: [%s]",
                paymentProviderServiceName, transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnSuccess);
    }

    private void handleNotPendingTransactionServiceDepositTransaction(
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceDepositTransaction.getId();

        TransactionStatus transactionServiceDepositTransactionStatus = transactionServiceDepositTransaction.getStatus();

        switch (transactionServiceDepositTransactionStatus) {
            case CANCELED, COMPLETED -> {
                String logMessageOnWarn = String.format(
                        "Attempt to handle deposit transaction with id: [%s] in status: [%s] " +
                        "during process operation: [%s]. Transaction will be marked in: [%s] status",
                        transactionId, transactionServiceDepositTransactionStatus, action, TransactionStatus.FAILED);

                log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

                applicationMetricsRegistry.countAction(false, action);

                transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);

                transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);
            }
            case FAILED -> {
                String logMessageOnWarn = String.format(
                        "Attempt of handle deposit transaction with id: [%s] in status: [%s] " +
                        "during process operation: [%s]",
                        transactionId, transactionServiceDepositTransactionStatus, action);

                log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

                applicationMetricsRegistry.countAction(false, action);
            }
        }
    }

//    --------------------------------------------------------------------------------------------------
//    -------------------------------------- WITHDRAW TRANSACTION --------------------------------------
//    --------------------------------------------------------------------------------------------------
    private void handleTransactionServiceWithdrawTransaction(
            PaymentProviderServiceErrorHandleTransactionDto paymentProviderServiceErrorHandleTransactionDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = paymentProviderServiceErrorHandleTransactionDto.getTransactionId();

        Optional<TransactionServiceWithdrawTransaction> transactionServiceWithdrawTransaction = transactionServiceWithdrawTransactionRepository.findById(transactionId);

        if(transactionServiceWithdrawTransaction.isPresent()) {
            handleFoundTransactionServiceWithdrawTransaction(
                    transactionServiceWithdrawTransaction.get(),
                    traceId,
                    spanId,
                    action
            );
        } else {
            handleNotFoundTransactionServiceTransactionEntity(
                    paymentProviderServiceErrorHandleTransactionDto,
                    traceId,
                    spanId,
                    action
            );
        }
    }

    private void handleFoundTransactionServiceWithdrawTransaction(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        if(transactionServiceWithdrawTransaction.getStatus() == TransactionStatus.PENDING) {
            handlePendingTransactionServiceWithdrawTransaction(
                    transactionServiceWithdrawTransaction,
                    traceId,
                    spanId,
                    action
            );
        } else {
            handleNotPendingTransactionServiceWithdrawTransaction(
                    transactionServiceWithdrawTransaction,
                    traceId,
                    spanId,
                    action
            );
        }
    }

    private void handlePendingTransactionServiceWithdrawTransaction(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceWithdrawTransaction.getId();

        transactionServiceWithdrawTransaction.setStatus(TransactionStatus.FAILED);

        transactionServiceWithdrawTransactionRepository.save(transactionServiceWithdrawTransaction);

        applicationMetricsRegistry.countAction(true, action);

        String logMessageOnSuccess = String.format(
                "Success handle of fail response from service: [%s] of withdraw transaction with id: [%s]",
                paymentProviderServiceName, transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnSuccess);
    }

    private void handleNotPendingTransactionServiceWithdrawTransaction(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceWithdrawTransaction.getId();

        TransactionStatus transactionServiceWithdrawTransactionStatus = transactionServiceWithdrawTransaction.getStatus();

        switch (transactionServiceWithdrawTransactionStatus) {
            case CANCELED, COMPLETED -> {
                String logMessageOnWarn = String.format(
                        "Attempt to handle withdraw transaction with id: [%s] in status: [%s] " +
                        "during process operation: [%s]. Transaction will be marked in: [%s] status",
                        transactionId, transactionServiceWithdrawTransactionStatus, action, TransactionStatus.FAILED);

                log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

                applicationMetricsRegistry.countAction(false, action);

                transactionServiceWithdrawTransaction.setStatus(TransactionStatus.FAILED);

                transactionServiceWithdrawTransactionRepository.save(transactionServiceWithdrawTransaction);
            }
            case FAILED -> {
                String logMessageOnWarn = String.format(
                        "Attempt of handle withdraw transaction with id: [%s] in status: [%s] " +
                        "during process operation: [%s]",
                        transactionId, transactionServiceWithdrawTransactionStatus, action);

                log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

                applicationMetricsRegistry.countAction(false, action);
            }
        }
    }

//    --------------------------------------------------------------------------------------------------
//    -------------------------------------- TRANSFER TRANSACTION --------------------------------------
//    --------------------------------------------------------------------------------------------------
    private void handleTransactionServiceTransferTransaction(
            PaymentProviderServiceErrorHandleTransactionDto paymentProviderServiceErrorHandleTransactionDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = paymentProviderServiceErrorHandleTransactionDto.getTransactionId();

        Optional<TransactionServiceTransferTransaction> transactionServiceTransferTransaction = transactionServiceTransferTransactionRepository.findById(transactionId);

        if(transactionServiceTransferTransaction.isPresent()) {
            handleFoundTransactionServiceTransferTransaction(
                    transactionServiceTransferTransaction.get(),
                    traceId,
                    spanId,
                    action
            );
        } else {
            handleNotFoundTransactionServiceTransactionEntity(
                    paymentProviderServiceErrorHandleTransactionDto,
                    traceId,
                    spanId,
                    action
            );
        }
    }
    private void handleFoundTransactionServiceTransferTransaction(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        if(transactionServiceTransferTransaction.getStatus() == TransactionStatus.PENDING) {
            handlePendingTransactionServiceTransferTransaction(
                    transactionServiceTransferTransaction,
                    traceId,
                    spanId,
                    action
            );
        } else {
            handleNotPendingTransactionServiceTransferTransaction(
                    transactionServiceTransferTransaction,
                    traceId,
                    spanId,
                    action
            );
        }
    }

    private void handlePendingTransactionServiceTransferTransaction(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceTransferTransaction.getId();

        transactionServiceTransferTransaction.setStatus(TransactionStatus.FAILED);

        transactionServiceTransferTransactionRepository.save(transactionServiceTransferTransaction);

        applicationMetricsRegistry.countAction(true, action);

        String logMessageOnSuccess = String.format(
                "Success handle of fail response from service: [%s] of transfer transaction with id: [%s]",
                paymentProviderServiceName, transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnSuccess);
    }

    private void handleNotPendingTransactionServiceTransferTransaction(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceTransferTransaction.getId();

        TransactionStatus transactionServiceTransferTransactionStatus = transactionServiceTransferTransaction.getStatus();

        switch (transactionServiceTransferTransactionStatus) {
            case CANCELED, COMPLETED -> {
                String logMessageOnWarn = String.format(
                        "Attempt to handle transfer transaction with id: [%s] in status: [%s] " +
                        "during process operation: [%s]. Transaction will be marked in: [%s] status",
                        transactionId, transactionServiceTransferTransactionStatus, action, TransactionStatus.FAILED);

                log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

                applicationMetricsRegistry.countAction(false, action);

                transactionServiceTransferTransaction.setStatus(TransactionStatus.FAILED);

                transactionServiceTransferTransactionRepository.save(transactionServiceTransferTransaction);
            }
            case FAILED -> {
                String logMessageOnWarn = String.format(
                        "Attempt of handle transfer transaction with id: [%s] in status: [%s] " +
                        "during process operation: [%s]",
                        transactionId, transactionServiceTransferTransactionStatus, action);

                log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

                applicationMetricsRegistry.countAction(false, action);
            }
        }
    }

    private void handleNotFoundTransactionServiceTransactionEntity(
            PaymentProviderServiceErrorHandleTransactionDto paymentProviderServiceErrorHandleTransactionDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = paymentProviderServiceErrorHandleTransactionDto.getTransactionId();

        String logMessageOnError = String.format(
                "Fail handle transaction with id: [%s]. Transaction not found",
                transactionId);

        log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnError);

        applicationMetricsRegistry.countAction(false, action);
    }
}
