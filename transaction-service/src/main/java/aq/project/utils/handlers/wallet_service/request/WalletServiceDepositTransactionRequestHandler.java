package aq.project.utils.handlers.wallet_service.request;

import aq.project.dto.*;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
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
public class WalletServiceDepositTransactionRequestHandler {

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

    private final TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    private final WalletServiceDepositTransactionRequestRepository walletServiceDepositTransactionRequestRepository;

    @Transactional
    public void handleTransactionRequest(
            @NotNull @Valid WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest
    ) {
        UUID transactionId = walletServiceDepositTransactionRequest.getTransactionId();

        String traceId = walletServiceDepositTransactionRequest.getTransactionRequestMetadata().getTraceId();
        String operation = Operation.DEPOSIT.name().toLowerCase();
        String action = String.format("schedule-%s-transaction-request-by-wallet-service", operation);
        String tracerName = serviceName + "." + action + "-tracer";

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String spanId = span.getSpanContext().getSpanId();
        String logMessageOnReceive = String.format(
                "Schedule handle [deposit] transaction with id: [%s] by [wallet-service]",
                transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        Timer.Sample sample = applicationMetricsRegistry.startTimer();

        Optional<TransactionServiceDepositTransaction> transactionServiceDepositTransactionOptional = transactionServiceDepositTransactionRepository.findById(transactionId);

        try(Scope scope = span.makeCurrent()) {
            if(transactionServiceDepositTransactionOptional.isPresent()) {
                handleFoundTransactionServiceDepositTransaction(
                        walletServiceDepositTransactionRequest,
                        transactionServiceDepositTransactionOptional.get(),
                        traceId,
                        spanId,
                        action
                );
            } else {
                handleNotFoundTransactionServiceDepositTransaction(
                        walletServiceDepositTransactionRequest,
                        traceId,
                        spanId,
                        action
                );
            }
        } finally {
            walletServiceDepositTransactionRequest.getTransactionRequestMetadata().setProcessed(true);
            walletServiceDepositTransactionRequestRepository.save(walletServiceDepositTransactionRequest);
            applicationMetricsRegistry.finishTimer(sample, action);
            span.end();
        }
    }

    private void handleFoundTransactionServiceDepositTransaction(
            WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest,
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        TransactionStatus transactionServiceDepositTransactionStatus = transactionServiceDepositTransaction.getStatus();

        if(transactionServiceDepositTransactionStatus == TransactionStatus.PENDING) {
            handlePendingTransactionServiceDepositTransaction(
                    walletServiceDepositTransactionRequest,
                    transactionServiceDepositTransaction,
                    traceId,
                    spanId,
                    action
            );
        } else {
            applicationMetricsRegistry.countAction(false, action);

            handleNotPendingTransactionServiceDepositTransaction(
                    transactionServiceDepositTransaction,
                    traceId,
                    spanId,
                    action
            );
//          TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
        }
    }

    private void handlePendingTransactionServiceDepositTransaction(
            WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest,
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceDepositTransaction.getId();

        try {
            String walletServiceDepositTransactionRequestTopic = walletServiceKafkaProperties
                    .getTopic("wallet_service_create_deposit_transaction_request");
            try {
                WalletServiceDepositTransactionRequestDto depositTransactionRequestWalletServiceDto = walletServiceTransactionDtoMapper.toDepositTransactionRequestWalletServiceDto(walletServiceDepositTransactionRequest);

                kafkaTemplate.send(walletServiceDepositTransactionRequestTopic, depositTransactionRequestWalletServiceDto).get();

                applicationMetricsRegistry.countAction(true, action);
            } catch (InterruptedException | ExecutionException e) {
                String logMessageOnError = String.format(
                        "Error occurred while sending [deposit] transaction with id: [%s] to kafka-topic: [%s] for service: [%s]",
                        transactionId, walletServiceDepositTransactionRequestTopic, walletServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                applicationMetricsRegistry.countAction(false, action);

                transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);
                transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

                sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                        walletServiceDepositTransactionRequest,
                        traceId,
                        spanId,
                        action
                );
//              TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while schedule handle [deposit] transaction with id: [%s] by [wallet-service]: " +
                    "topic with name: [%s] wasn't found",
                    transactionId, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnError);

            applicationMetricsRegistry.countAction(false, action);

            transactionServiceDepositTransaction.setStatus(TransactionStatus.FAILED);
            transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);

            sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                    walletServiceDepositTransactionRequest,
                    traceId,
                    spanId,
                    action
            );
//          TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
        }
    }

    private void handleNotFoundTransactionServiceDepositTransaction(
            WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceDepositTransactionRequest.getTransactionId();

        String logMessageOnError = String.format(
                "Fail handle [deposit] transaction with id: [%s]. Transaction not found",
                transactionId);

        log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnError);

        applicationMetricsRegistry.countAction(false, action);

        sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                walletServiceDepositTransactionRequest,
                traceId,
                spanId,
                action
        );
    }

    private void handleNotPendingTransactionServiceDepositTransaction(
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceDepositTransaction.getId();

        TransactionStatus transactionServiceDepositTransactionStatus = transactionServiceDepositTransaction.getStatus();

        String logMessageOnWarn = String.format(
                "Attempt to handle [deposit] transaction with id: [%s] in [%s] status " +
                "during process operation: [%s]",
                transactionId, transactionServiceDepositTransactionStatus, action);

        log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

        switch(transactionServiceDepositTransactionStatus) {
            case FAILED -> sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                    transactionServiceDepositTransaction,
                    traceId,
                    spanId,
                    action
            );
            case CANCELED -> sendCancelTransactionRequestPaymentProviderServiceDtoToKafka(
                    transactionServiceDepositTransaction,
                    traceId,
                    spanId,
                    action
            );
        }
    }

    private void sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
            WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceDepositTransactionRequest.getTransactionId();

        try {
            String paymentProviderServiceFailTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_fail_transaction_request");

            PaymentProviderServiceFailTransactionRequestDto paymentProviderServiceFailTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(walletServiceDepositTransactionRequest);
            paymentProviderServiceFailTransactionRequestDto.setOperation(Operation.DEPOSIT);
            paymentProviderServiceFailTransactionRequestDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceFailTransactionRequestDto).get();
            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [deposit] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending fail [deposit] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), paymentProviderServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }

    private void sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceDepositTransaction.getId();

        try {
            String paymentProviderServiceFailTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_fail_transaction_request");

            PaymentProviderServiceFailTransactionRequestDto paymentProviderServiceFailTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(transactionServiceDepositTransaction);
            paymentProviderServiceFailTransactionRequestDto.setOperation(Operation.DEPOSIT);
            paymentProviderServiceFailTransactionRequestDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceFailTransactionRequestDto).get();
            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [deposit] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending fail [deposit] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), paymentProviderServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }

    private void sendCancelTransactionRequestPaymentProviderServiceDtoToKafka(
            TransactionServiceDepositTransaction transactionServiceDepositTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceDepositTransaction.getId();

        try {
            String paymentProviderServiceCancelTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_cancel_transaction_request");

            PaymentProviderServiceCancelTransactionRequestDto paymentProviderServiceCancelTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceCancelTransactionRequestDto(transactionServiceDepositTransaction);
            paymentProviderServiceCancelTransactionRequestDto.setOperation(Operation.DEPOSIT);
            paymentProviderServiceCancelTransactionRequestDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceCancelTransactionRequestDto).get();
            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending cancel [deposit] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending cancel [deposit] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), paymentProviderServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }
}
