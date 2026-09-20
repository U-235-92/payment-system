package aq.project.utils.handlers.wallet_service.request;

import aq.project.dto.*;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceTransferTransactionRequestRepository;
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
public class WalletServiceTransferTransactionRequestHandler {

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

    private final TransactionServiceTransferTransactionRepository transactionServiceTransferTransactionRepository;
    private final WalletServiceTransferTransactionRequestRepository walletServiceTransferTransactionRequestRepository;

    @Transactional
    public void handleTransactionRequest(
            @NotNull @Valid WalletServiceTransferTransactionRequest walletServiceTransferTransactionRequest
    ) {
        UUID transactionId = walletServiceTransferTransactionRequest.getTransactionId();

        String traceId = walletServiceTransferTransactionRequest.getTransactionRequestMetadata().getTraceId();
        String operation = Operation.TRANSFER.name().toLowerCase();
        String action = String.format("schedule-%s-transaction-request-by-wallet-service", operation);
        String tracerName = serviceName + "." + action + "-tracer";

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String spanId = span.getSpanContext().getSpanId();
        String logMessageOnReceive = String.format(
                "Schedule handle [transfer] transaction with id: [%s] by [wallet-service]",
                transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        Timer.Sample sample = applicationMetricsRegistry.startTimer();

        Optional<TransactionServiceTransferTransaction> transactionServiceTransferTransactionOptional = transactionServiceTransferTransactionRepository.findById(transactionId);

        try(Scope scope = span.makeCurrent()) {
            if(transactionServiceTransferTransactionOptional.isPresent()) {
                handleFoundTransactionServiceTransferTransaction(
                        walletServiceTransferTransactionRequest,
                        transactionServiceTransferTransactionOptional.get(),
                        traceId,
                        spanId,
                        action
                );
            } else {
                handleNotFoundTransactionServiceTransferTransaction(
                        walletServiceTransferTransactionRequest,
                        traceId,
                        spanId,
                        action
                );
            }
        } finally {
            walletServiceTransferTransactionRequest.getTransactionRequestMetadata().setProcessed(true);
            walletServiceTransferTransactionRequestRepository.save(walletServiceTransferTransactionRequest);
            applicationMetricsRegistry.finishTimer(sample, action);
            span.end();
        }
    }

    private void handleFoundTransactionServiceTransferTransaction(
            WalletServiceTransferTransactionRequest walletServiceTransferTransactionRequest,
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        TransactionStatus transactionServiceTransferTransactionStatus = transactionServiceTransferTransaction.getStatus();

        if(transactionServiceTransferTransactionStatus == TransactionStatus.PENDING) {
            handlePendingTransactionServiceTransferTransaction(
                    walletServiceTransferTransactionRequest,
                    transactionServiceTransferTransaction,
                    traceId,
                    spanId,
                    action
            );
        } else {
            applicationMetricsRegistry.countAction(false, action);

            handleNotPendingTransactionServiceTransferTransaction(
                    transactionServiceTransferTransaction,
                    traceId,
                    spanId,
                    action
            );
//          TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
        }
    }

    private void handlePendingTransactionServiceTransferTransaction(
            WalletServiceTransferTransactionRequest walletServiceTransferTransactionRequest,
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceTransferTransaction.getId();

        try {
            String walletServiceTransferTransactionRequestTopic = walletServiceKafkaProperties
                    .getTopic("wallet_service_create_transfer_transaction_request");
            try {
                WalletServiceTransferTransactionRequestDto transferTransactionRequestDto = walletServiceTransactionDtoMapper.toTransferTransactionRequestWalletServiceDto(walletServiceTransferTransactionRequest);

                kafkaTemplate.send(walletServiceTransferTransactionRequestTopic, transferTransactionRequestDto).get();

                applicationMetricsRegistry.countAction(true, action);

            } catch (InterruptedException | ExecutionException e) {
                String logMessageOnError = String.format(
                        "Error occurred while sending [transfer] transaction with id: [%s] to kafka-topic: [%s] for service: [%s]",
                        transactionId, walletServiceTransferTransactionRequestTopic, walletServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                applicationMetricsRegistry.countAction(false, action);

                transactionServiceTransferTransaction.setStatus(TransactionStatus.FAILED);
                transactionServiceTransferTransactionRepository.save(transactionServiceTransferTransaction);

                sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                        walletServiceTransferTransactionRequest,
                        traceId,
                        spanId,
                        action
                );
//              TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while schedule handle [transfer] transaction with id: [%s] by [wallet-service]: " +
                    "topic with name: [%s] wasn't found",
                    transactionId, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnError);

            applicationMetricsRegistry.countAction(false, action);

            transactionServiceTransferTransaction.setStatus(TransactionStatus.FAILED);
            transactionServiceTransferTransactionRepository.save(transactionServiceTransferTransaction);

            sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                    walletServiceTransferTransactionRequest,
                    traceId,
                    spanId,
                    action
            );
//          TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
        }
    }

    private void handleNotFoundTransactionServiceTransferTransaction(
            WalletServiceTransferTransactionRequest walletServiceTransferTransactionRequest,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceTransferTransactionRequest.getTransactionId();

        String logMessageOnError = String.format(
                "Fail handle [transfer] transaction with id: [%s]. Transaction not found",
                transactionId);

        log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnError);

        applicationMetricsRegistry.countAction(false, action);

        sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                walletServiceTransferTransactionRequest,
                traceId,
                spanId,
                action
        );
    }

    private void handleNotPendingTransactionServiceTransferTransaction(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceTransferTransaction.getId();

        TransactionStatus transactionServiceTransferTransactionStatus = transactionServiceTransferTransaction.getStatus();

        String logMessageOnWarn = String.format(
                "Attempt to handle [transfer] transaction with id: [%s] in [%s] status " +
                "during process operation: [%s]",
                transactionId, transactionServiceTransferTransactionStatus, action);

        log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

        switch(transactionServiceTransferTransactionStatus) {
            case FAILED -> sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                    transactionServiceTransferTransaction,
                    traceId,
                    spanId,
                    action
            );
            case CANCELED -> sendCancelTransactionRequestPaymentProviderServiceDtoToKafka(
                    transactionServiceTransferTransaction,
                    traceId,
                    spanId,
                    action
            );
        }
    }

    private void sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
            WalletServiceTransferTransactionRequest walletServiceTransferTransactionRequest,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceTransferTransactionRequest.getTransactionId();

        try {
            String paymentProviderServiceFailTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_fail_transaction_request");

            PaymentProviderServiceFailTransactionRequestDto paymentProviderServiceFailTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(walletServiceTransferTransactionRequest);
            paymentProviderServiceFailTransactionRequestDto.setOperation(Operation.TRANSFER);
            paymentProviderServiceFailTransactionRequestDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceFailTransactionRequestDto).get();

            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [transfer] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending fail [transfer] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), paymentProviderServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }

    private void sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceTransferTransaction.getId();

        try {
            String paymentProviderServiceFailTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_fail_transaction_request");

            PaymentProviderServiceFailTransactionRequestDto paymentProviderServiceFailTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(transactionServiceTransferTransaction);
            paymentProviderServiceFailTransactionRequestDto.setOperation(Operation.TRANSFER);
            paymentProviderServiceFailTransactionRequestDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceFailTransactionRequestDto).get();
            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [transfer] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending fail [transfer] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), paymentProviderServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }

    private void sendCancelTransactionRequestPaymentProviderServiceDtoToKafka(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceTransferTransaction.getId();

        try {
            String paymentProviderServiceCancelTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_cancel_transaction_request");

            PaymentProviderServiceCancelTransactionRequestDto paymentProviderServiceCancelTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceCancelTransactionRequestDto(transactionServiceTransferTransaction);
            paymentProviderServiceCancelTransactionRequestDto.setOperation(Operation.TRANSFER);
            paymentProviderServiceCancelTransactionRequestDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceCancelTransactionRequestDto).get();
            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending cancel [transfer] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending cancel [transfer] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), paymentProviderServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }
}
