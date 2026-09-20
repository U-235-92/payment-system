package aq.project.utils.handlers.wallet_service.response.transfer_transaction;

import aq.project.dto.*;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.properties.PaymentProviderServiceKafkaProperties;
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
public class WalletServiceFailResponseCreateTransferTransactionHandler {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${service.payment-provider-service.name}")
    private String paymentProviderServiceName;
    @Value("${service.transaction-service.merchant-id}")
    private String merchantId;

    private final PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    private final PaymentProviderServiceKafkaProperties paymentProviderServiceKafkaProperties;

    private final TransactionServiceTransferTransactionRepository transactionServiceTransferTransactionRepository;

    @Transactional
    @KafkaListener(topics = "wallet_service_create_transfer_transaction_response_exceptions")
    public void handleErrorResponseCreateTransactionOnWalletService(
            @NotNull @Valid WalletServiceTransactionErrorResponseDto walletServiceTransactionErrorResponseDto
    ) {
        UUID transactionId = walletServiceTransactionErrorResponseDto.getTransactionId();

        String traceId = walletServiceTransactionErrorResponseDto.getTraceId();
        String operation = Operation.TRANSFER.name().toLowerCase();
        String action = String.format("handle-error-response-create-%s-transaction-on-wallet-service", operation);
        String tracerName = serviceName + "." + action + "-tracer";

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String spanId = span.getSpanContext().getSpanId();
        String logMessageOnReceive = String.format(
                "Received error response of created transfer transaction with id: [%s] on wallet service",
                transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        Timer.Sample sample = applicationMetricsRegistry.startTimer();

        Optional<TransactionServiceTransferTransaction> transactionServiceTransferTransactionOptional = transactionServiceTransferTransactionRepository.findById(transactionId);

        try(Scope scope = span.makeCurrent()) {
            if(transactionServiceTransferTransactionOptional.isPresent()) {
                handleFoundTransactionServiceTransferTransaction(
                        transactionServiceTransferTransactionOptional.get(),
                        traceId,
                        spanId,
                        action
                );
            } else {
                handleNotFoundTransactionServiceTransferTransaction(
                        walletServiceTransactionErrorResponseDto,
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

    private void handleFoundTransactionServiceTransferTransaction(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        TransactionStatus transactionServiceTransferTransactionStatus = transactionServiceTransferTransaction.getStatus();

        if(transactionServiceTransferTransactionStatus == TransactionStatus.PENDING) {
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

        TransactionStatus currentTransactionStatus = transactionServiceTransferTransaction.getStatus();
        TransactionStatus targetTransactionStatus = TransactionStatus.FAILED;

        transactionServiceTransferTransaction.setStatus(targetTransactionStatus);
        transactionServiceTransferTransactionRepository.save(transactionServiceTransferTransaction);

        String logMessageOnReceive = String.format(
                "Success handle transfer transaction with id: [%s]. Transaction status was changed from: [%s] to [%s]",
                transactionId, currentTransactionStatus, targetTransactionStatus);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        applicationMetricsRegistry.countAction(true, action);
    }

    private void handleNotPendingTransactionServiceTransferTransaction(
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceTransferTransaction.getId();

        TransactionStatus currentTransactionStatus = transactionServiceTransferTransaction.getStatus();
        TransactionStatus targetTransactionStatus = TransactionStatus.FAILED;

        String logMessageOnError = String.format(
                "Error occurred while handle transfer transaction with id: [%s]. " +
                "Attempt to change transaction status from: [%s] to [%s] which is prohibited",
                transactionId, currentTransactionStatus, targetTransactionStatus);

        log.error("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnError);

        applicationMetricsRegistry.countAction(true, action);

        switch (currentTransactionStatus) {
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
            TransactionServiceTransferTransaction transactionServiceTransferTransaction,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = transactionServiceTransferTransaction.getId();

        try {
            String paymentProviderServiceFailTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_fail_transaction_request");

            PaymentProviderServiceFailTransactionRequestDto failTransactionRequestPaymentProviderServiceDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(transactionServiceTransferTransaction);
            failTransactionRequestPaymentProviderServiceDto.setOperation(Operation.TRANSFER);
            failTransactionRequestPaymentProviderServiceDto.setMerchantId(merchantId);

            try {
                String logMessageOnSend = String.format(
                        "Attempt to send failure [transfer] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnSend);

                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, failTransactionRequestPaymentProviderServiceDto).get();

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

            PaymentProviderServiceCancelTransactionRequestDto cancelTransactionRequestPaymentProviderServiceDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceCancelTransactionRequestDto(transactionServiceTransferTransaction);
            cancelTransactionRequestPaymentProviderServiceDto.setOperation(Operation.TRANSFER);
            cancelTransactionRequestPaymentProviderServiceDto.setMerchantId(merchantId);

            try {
                String logMessageOnSend = String.format(
                        "Attempt to send cancel [transfer] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceName);

                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnSend);

                kafkaTemplate.send(paymentProviderServiceCancelTransactionRequestTopic, cancelTransactionRequestPaymentProviderServiceDto).get();

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

    private void handleNotFoundTransactionServiceTransferTransaction(
            WalletServiceTransactionErrorResponseDto walletServiceTransactionErrorResponseDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceTransactionErrorResponseDto.getTransactionId();

        String logMessageOnError = String.format(
                "Error occurred while handle [transfer] transaction with id: [%s]. Transaction not found",
                transactionId);

        log.error("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnError);

        sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
                walletServiceTransactionErrorResponseDto,
                traceId,
                spanId,
                action
        );
    }

    private void sendFailTransactionRequestPaymentProviderServiceDtoToKafka(
            WalletServiceTransactionErrorResponseDto walletServiceTransactionErrorResponseDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = walletServiceTransactionErrorResponseDto.getTransactionId();

        try {
            String paymentProviderServiceFailTransactionRequestTopic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_fail_transaction_request");

            PaymentProviderServiceFailTransactionRequestDto failTransactionRequestPaymentProviderServiceDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(walletServiceTransactionErrorResponseDto);
            failTransactionRequestPaymentProviderServiceDto.setOperation(Operation.TRANSFER);
            failTransactionRequestPaymentProviderServiceDto.setMerchantId(merchantId);

            try {
                String logMessageOnSend = String.format(
                        "Attempt to send failure [transfer] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.info("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnSend);

                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, failTransactionRequestPaymentProviderServiceDto).get();

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
}
