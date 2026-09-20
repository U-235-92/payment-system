package aq.project.utils.handlers.wallet_service.request;

import aq.project.dto.*;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceWithdrawTransactionRequestRepository;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.mappers.WalletServiceTransactionDtoMapper;
import aq.project.utils.properties.PaymentProviderServiceKafkaProperties;
import aq.project.utils.properties.WalletServiceKafkaProperties;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class WalletServiceWithdrawTransactionRequestHandler {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${service.transaction-service.merchant-id}")
    private String merchantId;
    @Value("${service.payment-provider-service.name}")
    private String paymentProviderServiceName;
    @Value("${service.wallet-service.name}")
    private String walletServiceName;

    private final WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;
    private final PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    private final PaymentProviderServiceKafkaProperties paymentProviderServiceKafkaProperties;
    private final WalletServiceKafkaProperties walletServiceKafkaProperties;

    private final TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    private final WalletServiceWithdrawTransactionRequestRepository walletServiceWithdrawTransactionRequestRepository;

    @Transactional
    public void handleTransactionRequest(
            @NotNull @Valid  WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest
    ) {
        UUID transactionId = walletServiceWithdrawTransactionRequest.getTransactionId();

        String traceId = walletServiceWithdrawTransactionRequest.getTransactionRequestMetadata().getTraceId();
        String operation = Operation.WITHDRAW.name().toLowerCase();
        String action = String.format("schedule-%s-transaction-request-by-wallet-service", operation);
        String tracerName = serviceName + "." + action + "-tracer";

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String spanId = span.getSpanContext().getSpanId();
        String logMessageOnReceive = String.format(
                "Schedule handle [withdraw] transaction with id: [%s] by [wallet-service]",
                transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        Timer.Sample sample = applicationMetricsRegistry.startTimer();

        Optional<TransactionServiceWithdrawTransaction> transactionServiceWithdrawTransactionOptional = transactionServiceWithdrawTransactionRepository.findById(transactionId);

        try(Scope scope = span.makeCurrent()) {
            if(transactionServiceWithdrawTransactionOptional.isPresent()) {
                handleFoundTransactionServiceWithdrawTransaction(
                        walletServiceWithdrawTransactionRequest,
                        transactionServiceWithdrawTransactionOptional.get(),
                        traceId,
                        spanId,
                        action
                );
            } else {
                handleNotFoundTransactionServiceWithdrawTransaction(
                        walletServiceWithdrawTransactionRequest,
                        traceId,
                        spanId,
                        action
                );
            }
        } finally {
            walletServiceWithdrawTransactionRequest.getTransactionRequestMetadata().setProcessed(true);
            walletServiceWithdrawTransactionRequestRepository.save(walletServiceWithdrawTransactionRequest);
            applicationMetricsRegistry.finishTimer(sample, action);
            span.end();
        }
    }

    private void handleFoundTransactionServiceWithdrawTransaction(
            WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest,
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        TransactionStatus transactionServiceWithdrawTransactionStatus = transactionServiceWithdrawTransaction.getStatus();

        if(transactionServiceWithdrawTransactionStatus == TransactionStatus.PENDING) {
            handlePendingTransactionServiceWithdrawTransaction(
                    walletServiceWithdrawTransactionRequest,
                    transactionServiceWithdrawTransaction,
                    traceId,
                    spanId,
                    action
            );
        } else {
            applicationMetricsRegistry.countAction(false, action);

            handleNotPendingTransactionServiceWithdrawTransaction(
                    transactionServiceWithdrawTransaction,
                    traceId,
                    spanId,
                    action
            );
//          TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
        }
    }

    private void handlePendingTransactionServiceWithdrawTransaction(
            WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest,
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceWithdrawTransaction.getId();

        try {
            String walletServiceCreateWithdrawTransactionRequest = walletServiceKafkaProperties
                    .getTopic("wallet_service_create_withdraw_transaction_request");
            try {
                WalletServiceWithdrawTransactionRequestDto withdrawTransactionRequestWalletServiceDto = walletServiceTransactionDtoMapper.toWithdrawTransactionRequestWalletServiceDto(walletServiceWithdrawTransactionRequest);

                kafkaTemplate.send(walletServiceCreateWithdrawTransactionRequest, withdrawTransactionRequestWalletServiceDto).get();

                applicationMetricsRegistry.countAction(true, action);
            } catch (InterruptedException | ExecutionException e) {
                String logMessageOnError = String.format(
                        "Error occurred while sending [withdraw] transaction with id: [%s] to kafka-topic: [%s] for service: [%s]",
                        transactionId, walletServiceCreateWithdrawTransactionRequest, walletServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                applicationMetricsRegistry.countAction(false, action);

                transactionServiceWithdrawTransaction.setStatus(TransactionStatus.FAILED);
                transactionServiceWithdrawTransactionRepository.save(transactionServiceWithdrawTransaction);

                sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                        walletServiceWithdrawTransactionRequest,
                        traceId,
                        spanId,
                        action
                );
//              TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while schedule handle [withdraw] transaction with id: [%s] by [wallet-service]: " +
                    "topic with name: [%s] wasn't found",
                    transactionId, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnError);

            applicationMetricsRegistry.countAction(false, action);

            transactionServiceWithdrawTransaction.setStatus(TransactionStatus.FAILED);
            transactionServiceWithdrawTransactionRepository.save(transactionServiceWithdrawTransaction);

            sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                    walletServiceWithdrawTransactionRequest,
                    traceId,
                    spanId,
                    action
            );
//          TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
        }
    }

    private void handleNotFoundTransactionServiceWithdrawTransaction(
            WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceWithdrawTransactionRequest.getTransactionId();

        String logMessageOnError = String.format(
                "Fail handle [withdraw] transaction with id: [%s]. Transaction not found",
                transactionId);

        log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnError);

        applicationMetricsRegistry.countAction(false, action);

        sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                walletServiceWithdrawTransactionRequest,
                traceId,
                spanId,
                action
        );
    }

    private void handleNotPendingTransactionServiceWithdrawTransaction(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceWithdrawTransaction.getId();

        TransactionStatus transactionServiceWithdrawTransactionStatus = transactionServiceWithdrawTransaction.getStatus();

        String logMessageOnWarn = String.format(
                "Attempt to handle [withdraw] transaction with id: [%s] in [%s] status " +
                "during process operation: [%s]",
                transactionId, transactionServiceWithdrawTransactionStatus, action);

        log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

        switch(transactionServiceWithdrawTransactionStatus) {
            case FAILED -> sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                    transactionServiceWithdrawTransaction,
                    traceId,
                    spanId,
                    action
            );
            case CANCELED -> sendCancelTransactionRequestPaymentProviderServiceDtoToKafka(
                    transactionServiceWithdrawTransaction,
                    traceId,
                    spanId,
                    action
            );
        }
    }

    private void sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
            WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceWithdrawTransactionRequest.getTransactionId();

        try {
            String paymentProviderServiceFailTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_fail_transaction_request");

            PaymentProviderServiceFailTransactionRequestDto paymentProviderServiceFailTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(walletServiceWithdrawTransactionRequest);
            paymentProviderServiceFailTransactionRequestDto.setOperation(Operation.WITHDRAW);
            paymentProviderServiceFailTransactionRequestDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceFailTransactionRequestDto).get();

            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending fail [withdraw] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), paymentProviderServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }

    private void sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceWithdrawTransaction.getId();

        try {
            String paymentProviderServiceFailTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_fail_transaction_request");

            PaymentProviderServiceFailTransactionRequestDto paymentProviderServiceFailTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(transactionServiceWithdrawTransaction);
            paymentProviderServiceFailTransactionRequestDto.setOperation(Operation.WITHDRAW);
            paymentProviderServiceFailTransactionRequestDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceFailTransactionRequestDto).get();
            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending fail [withdraw] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), paymentProviderServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }

    private void sendCancelTransactionRequestPaymentProviderServiceDtoToKafka(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceWithdrawTransaction.getId();

        try {
            String paymentProviderServiceCancelTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_cancel_transaction_request");

            PaymentProviderServiceCancelTransactionRequestDto paymentProviderServiceCancelTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceCancelTransactionRequestDto(transactionServiceWithdrawTransaction);
            paymentProviderServiceCancelTransactionRequestDto.setOperation(Operation.WITHDRAW);
            paymentProviderServiceCancelTransactionRequestDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceCancelTransactionRequestDto).get();
            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending cancel [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending cancel [withdraw] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), paymentProviderServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }
}
