package aq.project.utils.handlers.wallet_service.response.withdraw_trnsaction;

import aq.project.dto.*;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
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
import org.springframework.kafka.annotation.KafkaListener;
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
public class WalletServiceSuccessResponseCreateWithdrawTransactionHandler {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${service.payment-provider-service.name}")
    private String paymentProviderServiceName;
    @Value("${service.wallet-service.name}")
    private String walletServiceName;
    @Value("${service.transaction-service.merchant-id}")
    private String merchantId;

    private final WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;
    private final PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    private final PaymentProviderServiceKafkaProperties paymentProviderServiceKafkaProperties;
    private final WalletServiceKafkaProperties walletServiceKafkaProperties;

    private final TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;

    @Transactional
    @KafkaListener(topics = "wallet_service_create_withdraw_transaction_response")
    public void handleSuccessResponseCreateTransactionOnWalletService(
            @NotNull @Valid WalletServiceWithdrawTransactionSuccessResponseDto walletServiceWithdrawTransactionSuccessResponseDto
    ) {
        UUID transactionId = walletServiceWithdrawTransactionSuccessResponseDto.getTransactionId();

        String traceId = walletServiceWithdrawTransactionSuccessResponseDto.getTraceId();
        String operation = Operation.WITHDRAW.name().toLowerCase();
        String action = String.format("handle-success-response-create-%s-transaction-on-wallet-service", operation);
        String tracerName = serviceName + "." + action + "-tracer";

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String spanId = span.getSpanContext().getSpanId();
        String logMessageOnReceive = String.format(
                "Received success response of created withdraw transaction with id: [%s] on wallet service",
                transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        Timer.Sample sample = applicationMetricsRegistry.startTimer();

        Optional<TransactionServiceWithdrawTransaction> transactionServiceWithdrawTransactionOptional = transactionServiceWithdrawTransactionRepository.findById(transactionId);

        try(Scope scope = span.makeCurrent()) {
            if(transactionServiceWithdrawTransactionOptional.isPresent()) {
                handleFoundTransactionServiceWithdrawTransaction(
                        transactionServiceWithdrawTransactionOptional.get(),
                        traceId,
                        spanId,
                        action
                );
            } else {
                handleNotFoundTransactionServiceWithdrawTransaction(
                        walletServiceWithdrawTransactionSuccessResponseDto,
                        traceId,
                        spanId,
                        action
                );
            }
        } finally {
            applicationMetricsRegistry.finishTimer(sample, action);
            span.end();
        }
    }

    private void handleFoundTransactionServiceWithdrawTransaction(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        TransactionStatus transactionServiceWithdrawTransactionStatus = transactionServiceWithdrawTransaction.getStatus();
        if(transactionServiceWithdrawTransactionStatus == TransactionStatus.PENDING) {
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

        TransactionStatus currentTransactionStatus = transactionServiceWithdrawTransaction.getStatus();
        TransactionStatus targetTransactionStatus = TransactionStatus.COMPLETED;

        transactionServiceWithdrawTransaction.setStatus(targetTransactionStatus);
        transactionServiceWithdrawTransactionRepository.save(transactionServiceWithdrawTransaction);

        String logMessageOnReceive = String.format(
                "Success handle withdraw transaction with id: [%s]. Transaction status was changed from: [%s] to [%s]",
                transactionId, currentTransactionStatus, targetTransactionStatus);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        applicationMetricsRegistry.countAction(true, action);
    }

    private void handleNotPendingTransactionServiceWithdrawTransaction(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceWithdrawTransaction.getId();

        TransactionStatus currentTransactionStatus = transactionServiceWithdrawTransaction.getStatus();
        TransactionStatus targetTransactionStatus = TransactionStatus.COMPLETED;

        String logMessageOnError = String.format(
                "Error occurred while handle withdraw transaction with id: [%s]. " +
                "Attempt to change transaction status from: [%s] to [%s] which is prohibited",
                transactionId, currentTransactionStatus, targetTransactionStatus);

        log.error("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnError);

        applicationMetricsRegistry.countAction(true, action);

        switch (currentTransactionStatus) {
            case FAILED -> {
                sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                        transactionServiceWithdrawTransaction,
                        traceId,
                        spanId,
                        action
                );
                sendFailTransactionRequestWalletServiceDtoToKafka(
                        transactionServiceWithdrawTransaction,
                        traceId,
                        spanId,
                        action
                );
            }
            case CANCELED -> {
                sendCancelTransactionRequestPaymentProviderServiceDtoToKafka(
                        transactionServiceWithdrawTransaction,
                        traceId,
                        spanId,
                        action
                );
                sendCancelTransactionRequestWalletServiceDtoToKafka(
                        transactionServiceWithdrawTransaction,
                        traceId,
                        spanId,
                        action
                );
            }
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
                String logMessageOnSend = String.format(
                        "Attempt to send failure [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnSend);

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

    private void sendFailTransactionRequestWalletServiceDtoToKafka(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceWithdrawTransaction.getId();

        try {
            String walletServiceFailTransactionRequestTopic = walletServiceKafkaProperties
                    .getTopic("wallet_service_fail_withdraw_transaction_request");

            WalletServiceFailTransactionRequestDto walletServiceFailTransactionRequestDto = walletServiceTransactionDtoMapper.toWalletServiceFailTransactionRequestDto(transactionServiceWithdrawTransaction);

            try {
                String logMessageOnSend = String.format(
                        "Attempt to send failure [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, walletServiceFailTransactionRequestTopic, walletServiceName);

                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnSend);

                kafkaTemplate.send(walletServiceFailTransactionRequestTopic, walletServiceFailTransactionRequestDto).get();
            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, walletServiceFailTransactionRequestTopic, walletServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending fail [withdraw] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), walletServiceName, e.getTopic());

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
                String logMessageOnSend = String.format(
                        "Attempt to send cancel [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceName);

                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnSend);

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

    private void sendCancelTransactionRequestWalletServiceDtoToKafka(
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceWithdrawTransaction.getId();

        try {
            String walletServiceCancelTransactionRequestTopic = walletServiceKafkaProperties
                    .getTopic("wallet_service_cancel_withdraw_transaction_request");

            WalletServiceCancelTransactionRequestDto walletServiceCancelTransactionRequestDto = walletServiceTransactionDtoMapper.toWalletServiceWithdrawTransactionRequestDto(transactionServiceWithdrawTransaction);

            try {
                String logMessageOnSend = String.format(
                        "Attempt to send cancel [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, walletServiceCancelTransactionRequestTopic, walletServiceName);

                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnSend);

                kafkaTemplate.send(walletServiceCancelTransactionRequestTopic, walletServiceCancelTransactionRequestDto).get();

            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending cancel [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, walletServiceCancelTransactionRequestTopic, walletServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending cancel [withdraw] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), walletServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }

    private void handleNotFoundTransactionServiceWithdrawTransaction(
            WalletServiceWithdrawTransactionSuccessResponseDto walletServiceWithdrawTransactionSuccessResponseDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceWithdrawTransactionSuccessResponseDto.getTransactionId();

        String logMessageOnError = String.format(
                "Error occurred while handle [withdraw] transaction with id: [%s]. Transaction not found",
                transactionId);

        log.error("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnError);

        sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                walletServiceWithdrawTransactionSuccessResponseDto,
                traceId,
                spanId,
                action
        );
        sendFailTransactionRequestWalletServiceDtoToKafka(
                walletServiceWithdrawTransactionSuccessResponseDto,
                traceId,
                spanId,
                action
        );
    }

    private void sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
            WalletServiceWithdrawTransactionSuccessResponseDto walletServiceWithdrawTransactionSuccessResponseDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceWithdrawTransactionSuccessResponseDto.getTransactionId();

        try {
            String paymentProviderServiceFailTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_fail_transaction_request");

            PaymentProviderServiceFailTransactionRequestDto paymentProviderServiceFailTransactionRequestDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(walletServiceWithdrawTransactionSuccessResponseDto);
            paymentProviderServiceFailTransactionRequestDto.setOperation(Operation.WITHDRAW);
            paymentProviderServiceFailTransactionRequestDto.setMerchantId(merchantId);

            try {
                String logMessageOnSend = String.format(
                        "Attempt to send failure [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnSend);

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

    private void sendFailTransactionRequestWalletServiceDtoToKafka(
            WalletServiceWithdrawTransactionSuccessResponseDto walletServiceWithdrawTransactionSuccessResponseDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceWithdrawTransactionSuccessResponseDto.getTransactionId();

        try {
            String walletServiceFailTransactionRequestTopic = walletServiceKafkaProperties
                    .getTopic("wallet_service_fail_withdraw_transaction_request");

            WalletServiceFailTransactionRequestDto walletServiceFailTransactionRequestDto = walletServiceTransactionDtoMapper.toWalletServiceFailTransactionRequestDto(walletServiceWithdrawTransactionSuccessResponseDto);

            try {
                String logMessageOnSend = String.format(
                        "Attempt to send failure [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, walletServiceFailTransactionRequestTopic, walletServiceName);

                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnSend);

                kafkaTemplate.send(walletServiceFailTransactionRequestTopic, walletServiceFailTransactionRequestDto).get();
            } catch (ExecutionException | InterruptedException ex) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, walletServiceFailTransactionRequestTopic, walletServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, ex.getMessage());
            }
        } catch (NotFoundTopicException e) {
            String logMessageOnError = String.format(
                    "Error occurred while sending fail [withdraw] transaction request with transaction id: [%s] " +
                    "to kafka-topic: [%s] for service: [%s]. Topic with name: [%s] wasn't found",
                    transactionId, e.getTopic(), walletServiceName, e.getTopic());

            log.error("[{}-{}][{} -> {}]: {}",
                    traceId, spanId, serviceName, action, logMessageOnError);
        }
    }
}
