package aq.project.utils.handlers.payment_provider_service.response;

import aq.project.dto.*;
import aq.project.entities.transaction_service.TransactionServiceDepositTransaction;
import aq.project.entities.transaction_service.TransactionServiceTransferTransaction;
import aq.project.entities.transaction_service.TransactionServiceWithdrawTransaction;
import aq.project.entities.wallet_service.WalletServiceDepositTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceTransferTransactionRequest;
import aq.project.entities.wallet_service.WalletServiceWithdrawTransactionRequest;
import aq.project.exceptions.NotFoundTopicException;
import aq.project.repositories.transaction_service.TransactionServiceDepositTransactionRepository;
import aq.project.repositories.transaction_service.TransactionServiceTransferTransactionRepository;
import aq.project.repositories.transaction_service.TransactionServiceWithdrawTransactionRepository;
import aq.project.repositories.wallet_service.WalletServiceDepositTransactionRequestRepository;
import aq.project.repositories.wallet_service.WalletServiceTransferTransactionRequestRepository;
import aq.project.repositories.wallet_service.WalletServiceWithdrawTransactionRequestRepository;
import aq.project.utils.mappers.PaymentProviderTransactionDtoMapper;
import aq.project.utils.mappers.WalletServiceTransactionDtoMapper;
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
public class PaymentProviderServiceSuccessResponseCreateTransactionHandler {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${service.payment-provider-service.name}")
    private String paymentProviderServiceName;
    @Value("${service.transaction-service.merchant-id}")
    private String merchantId;

    private final PaymentProviderTransactionDtoMapper paymentProviderTransactionDtoMapper = PaymentProviderTransactionDtoMapper.INSTANCE;
    private final WalletServiceTransactionDtoMapper walletServiceTransactionDtoMapper = WalletServiceTransactionDtoMapper.INSTANCE;

    private final TransactionServiceDepositTransactionRepository transactionServiceDepositTransactionRepository;
    private final TransactionServiceWithdrawTransactionRepository transactionServiceWithdrawTransactionRepository;
    private final TransactionServiceTransferTransactionRepository transactionServiceTransferTransactionRepository;

    private final WalletServiceDepositTransactionRequestRepository walletServiceDepositTransactionRequestRepository;
    private final WalletServiceWithdrawTransactionRequestRepository walletServiceWithdrawTransactionRequestRepository;
    private final WalletServiceTransferTransactionRequestRepository walletServiceTransferTransactionRequestRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    private final PaymentProviderServiceKafkaProperties paymentProviderServiceKafkaProperties;

    @Transactional
    @KafkaListener(topics = "payment_provider_service_create_transaction_response")
    public void handleSuccessResponseCreateTransactionOnPaymentProviderService(
            @NotNull @Valid PaymentProviderServiceSuccessHandleTransactionDto paymentProviderServiceSuccessHandleTransactionDto
    ) {
        UUID transactionId = paymentProviderServiceSuccessHandleTransactionDto.getTransactionId();

        String traceId = paymentProviderServiceSuccessHandleTransactionDto.getTraceId();
        String operation = paymentProviderServiceSuccessHandleTransactionDto.getOperation().getValue().toLowerCase();
        String action = String.format("handle-success-response-create-%s-transaction-on-payment-provider-service", operation);
        String tracerName = serviceName + "." + action + "-tracer";

        Tracer tracer = openTelemetry.getTracer(tracerName);
        Span span = tracer.spanBuilder(action).startSpan();

        String spanId = span.getSpanContext().getSpanId();
        String logMessageOnReceive = String.format(
                "Received success response from service: [%s] after handle transaction with id: [%s]",
                paymentProviderServiceName, transactionId);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnReceive);

        Timer.Sample sample = applicationMetricsRegistry.startTimer();

        try(Scope scope = span.makeCurrent()) {
            Operation paymentProviderServiceTransactionOperation = paymentProviderServiceSuccessHandleTransactionDto
                    .getOperation();

            switch (paymentProviderServiceTransactionOperation) {
                case DEPOSIT -> handleTransactionServiceDepositTransaction(
                        paymentProviderServiceSuccessHandleTransactionDto,
                        traceId,
                        spanId,
                        action
                );
                case WITHDRAW -> handleTransactionServiceWithdrawTransaction(
                        paymentProviderServiceSuccessHandleTransactionDto,
                        traceId,
                        spanId,
                        action
                );
                case TRANSFER -> handleTransactionServiceTransferTransaction(
                        paymentProviderServiceSuccessHandleTransactionDto,
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
//    -------------------------------------------------------------------------------------------------
//    -------------------------------------- DEPOSIT TRANSACTION --------------------------------------
//    -------------------------------------------------------------------------------------------------
    private void handleTransactionServiceDepositTransaction(
            PaymentProviderServiceSuccessHandleTransactionDto paymentProviderServiceSuccessHandleTransactionDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = paymentProviderServiceSuccessHandleTransactionDto.getTransactionId();

        Optional<TransactionServiceDepositTransaction> transactionServiceDepositTransactionOptional = transactionServiceDepositTransactionRepository.findById(transactionId);

        if(transactionServiceDepositTransactionOptional.isPresent()) {
            TransactionServiceDepositTransaction transactionServiceDepositTransaction = transactionServiceDepositTransactionOptional.get();

            handleFoundTransactionServiceDepositTransaction(
                    transactionServiceDepositTransaction,
                    traceId,
                    spanId,
                    action
            );
        } else {
            handleNotFoundTransactionServiceTransaction(
                    paymentProviderServiceSuccessHandleTransactionDto,
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

        WalletServiceDepositTransactionRequest walletServiceDepositTransactionRequest = walletServiceTransactionDtoMapper.toWalletServiceDepositTransactionRequest(transactionServiceDepositTransaction);
        walletServiceDepositTransactionRequest.getTransactionRequestMetadata().setProcessed(false);

        walletServiceDepositTransactionRequestRepository.save(walletServiceDepositTransactionRequest);

        String logMessageOnSuccess = String.format(
                "Success handle deposit transaction with id: [%s] from service: [%s]",
                transactionId, paymentProviderServiceName);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnSuccess);

        applicationMetricsRegistry.countAction(true, action);
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
                "Attempt to handle deposit transaction with id: [%s] in [%s] status " +
                "during process operation: [%s]",
                transactionId, transactionServiceDepositTransactionStatus, action);

        log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

        applicationMetricsRegistry.countAction(false, action);

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
//      TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
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

            PaymentProviderServiceFailTransactionRequestDto failTransactionRequestPaymentProviderServiceDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(transactionServiceDepositTransaction);
            failTransactionRequestPaymentProviderServiceDto.setMerchantId(merchantId);
            failTransactionRequestPaymentProviderServiceDto.setOperation(Operation.DEPOSIT);

            try {
                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, failTransactionRequestPaymentProviderServiceDto).get();
            } catch (ExecutionException | InterruptedException e) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [deposit] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                transactionServiceDepositTransaction.setDescription(logMessageOnError);

                transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);
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

            PaymentProviderServiceCancelTransactionRequestDto cancelTransactionRequestPaymentProviderServiceDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceCancelTransactionRequestDto(transactionServiceDepositTransaction);
            cancelTransactionRequestPaymentProviderServiceDto.setOperation(Operation.DEPOSIT);
            cancelTransactionRequestPaymentProviderServiceDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceCancelTransactionRequestTopic, cancelTransactionRequestPaymentProviderServiceDto).get();
            } catch (ExecutionException | InterruptedException e) {
                String logMessageOnError = String.format(
                        "Error occurred while sending cancel [deposit] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                transactionServiceDepositTransaction.setDescription(logMessageOnError);

                transactionServiceDepositTransactionRepository.save(transactionServiceDepositTransaction);
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

//    --------------------------------------------------------------------------------------------------
//    -------------------------------------- WITHDRAW TRANSACTION --------------------------------------
//    --------------------------------------------------------------------------------------------------
    private void handleTransactionServiceWithdrawTransaction(
            PaymentProviderServiceSuccessHandleTransactionDto paymentProviderServiceSuccessHandleTransactionDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = paymentProviderServiceSuccessHandleTransactionDto.getTransactionId();

        Optional<TransactionServiceWithdrawTransaction> transactionServiceWithdrawTransactionOptional = transactionServiceWithdrawTransactionRepository.findById(transactionId);

        if(transactionServiceWithdrawTransactionOptional.isPresent()) {
            TransactionServiceWithdrawTransaction transactionServiceWithdrawTransaction = transactionServiceWithdrawTransactionOptional.get();

            handleFoundTransactionServiceWithdrawTransaction(
                    transactionServiceWithdrawTransaction,
                    traceId,
                    spanId,
                    action
            );
        } else {
            handleNotFoundTransactionServiceTransaction(
                    paymentProviderServiceSuccessHandleTransactionDto,
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

        WalletServiceWithdrawTransactionRequest walletServiceWithdrawTransactionRequest = walletServiceTransactionDtoMapper.toWalletServiceWithdrawTransactionRequest(transactionServiceWithdrawTransaction);
        walletServiceWithdrawTransactionRequest.getTransactionRequestMetadata().setProcessed(false);

        walletServiceWithdrawTransactionRequestRepository.save(walletServiceWithdrawTransactionRequest);

        String logMessageOnSuccess = String.format(
                "Success handle withdraw transaction with id: [%s] from service: [%s]",
                transactionId, paymentProviderServiceName);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnSuccess);

        applicationMetricsRegistry.countAction(true, action);
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
                "Attempt to handle withdraw transaction with id: [%s] in [%s] status " +
                "during process operation: [%s]",
                transactionId, transactionServiceWithdrawTransactionStatus, action);

        log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

        applicationMetricsRegistry.countAction(false, action);

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
//      TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
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

            PaymentProviderServiceFailTransactionRequestDto failTransactionRequestPaymentProviderServiceDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceFailTransactionRequestDto(transactionServiceWithdrawTransaction);
            failTransactionRequestPaymentProviderServiceDto.setMerchantId(merchantId);
            failTransactionRequestPaymentProviderServiceDto.setOperation(Operation.DEPOSIT);

            try {
                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, failTransactionRequestPaymentProviderServiceDto).get();
            } catch (ExecutionException | InterruptedException e) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                transactionServiceWithdrawTransaction.setDescription(logMessageOnError);

                transactionServiceWithdrawTransactionRepository.save(transactionServiceWithdrawTransaction);
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

            PaymentProviderServiceCancelTransactionRequestDto cancelTransactionRequestPaymentProviderServiceDto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceCancelTransactionRequestDto(transactionServiceWithdrawTransaction);
            cancelTransactionRequestPaymentProviderServiceDto.setOperation(Operation.DEPOSIT);
            cancelTransactionRequestPaymentProviderServiceDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceCancelTransactionRequestTopic, cancelTransactionRequestPaymentProviderServiceDto).get();
            } catch (ExecutionException | InterruptedException e) {
                String logMessageOnError = String.format(
                        "Error occurred while sending cancel [withdraw] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                transactionServiceWithdrawTransaction.setDescription(logMessageOnError);

                transactionServiceWithdrawTransactionRepository.save(transactionServiceWithdrawTransaction);
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

//    --------------------------------------------------------------------------------------------------
//    -------------------------------------- TRANSFER TRANSACTION --------------------------------------
//    --------------------------------------------------------------------------------------------------
    private void handleTransactionServiceTransferTransaction(
            PaymentProviderServiceSuccessHandleTransactionDto paymentProviderServiceSuccessHandleTransactionDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = paymentProviderServiceSuccessHandleTransactionDto.getTransactionId();

        Optional<TransactionServiceTransferTransaction> transactionServiceTransferTransactionOptional = transactionServiceTransferTransactionRepository.findById(transactionId);

        if(transactionServiceTransferTransactionOptional.isPresent()) {
            TransactionServiceTransferTransaction transactionServiceTransferTransaction = transactionServiceTransferTransactionOptional.get();

            handleFoundTransactionServiceTransferTransaction(
                    transactionServiceTransferTransaction,
                    traceId,
                    spanId,
                    action
            );
        } else {
            handleNotFoundTransactionServiceTransaction(
                    paymentProviderServiceSuccessHandleTransactionDto,
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

        WalletServiceTransferTransactionRequest walletServiceTransferTransactionRequest = walletServiceTransactionDtoMapper.toWalletServiceTransferTransactionRequest(transactionServiceTransferTransaction);
        walletServiceTransferTransactionRequest.getTransactionRequestMetadata().setProcessed(false);

        walletServiceTransferTransactionRequestRepository.save(walletServiceTransferTransactionRequest);

        String logMessageOnSuccess = String.format(
                "Success handle transfer transaction with id: [%s] from service: [%s]",
                transactionId, paymentProviderServiceName);

        log.info("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnSuccess);

        applicationMetricsRegistry.countAction(true, action);
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
                "Attempt to handle transfer transaction with id: [%s] in [%s] status " +
                "during process operation: [%s]",
                transactionId, transactionServiceTransferTransactionStatus, action);

        log.warn("[{}-{}][{} -> {}]: {}.", traceId, spanId, serviceName, action, logMessageOnWarn);

        applicationMetricsRegistry.countAction(false, action);

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
//      TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
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
            failTransactionRequestPaymentProviderServiceDto.setMerchantId(merchantId);
            failTransactionRequestPaymentProviderServiceDto.setOperation(Operation.DEPOSIT);

            try {
                kafkaTemplate.send(paymentProviderServiceFailTransactionRequestTopic, failTransactionRequestPaymentProviderServiceDto).get();
            } catch (ExecutionException | InterruptedException e) {
                String logMessageOnError = String.format(
                        "Error occurred while sending fail [transfer] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceFailTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                transactionServiceTransferTransaction.setDescription(logMessageOnError);

                transactionServiceTransferTransactionRepository.save(transactionServiceTransferTransaction);
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
            cancelTransactionRequestPaymentProviderServiceDto.setOperation(Operation.DEPOSIT);
            cancelTransactionRequestPaymentProviderServiceDto.setMerchantId(merchantId);

            try {
                kafkaTemplate.send(paymentProviderServiceCancelTransactionRequestTopic, cancelTransactionRequestPaymentProviderServiceDto).get();
            } catch (ExecutionException | InterruptedException e) {
                String logMessageOnError = String.format(
                        "Error occurred while sending cancel [transfer] transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, paymentProviderServiceCancelTransactionRequestTopic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

                transactionServiceTransferTransaction.setDescription(logMessageOnError);

                transactionServiceTransferTransactionRepository.save(transactionServiceTransferTransaction);
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

    private void handleNotFoundTransactionServiceTransaction(
            PaymentProviderServiceSuccessHandleTransactionDto paymentProviderServiceSuccessHandleTransactionDto,
            String traceId,
            String spanId,
            String action
    ) {
        UUID transactionId = paymentProviderServiceSuccessHandleTransactionDto.getTransactionId();

        String logMessageOnError = String.format(
                "Fail handle transaction with id: [%s]. Transaction not found", transactionId);

        log.error("[{}-{}][{} -> {}]: {}", traceId, spanId, serviceName, action, logMessageOnError);

        applicationMetricsRegistry.countAction(false, action);

        PaymentProviderServiceCancelTransactionRequestDto dto = paymentProviderTransactionDtoMapper.toPaymentProviderServiceCancelTransactionRequestDto(paymentProviderServiceSuccessHandleTransactionDto);

        try {
            String topic = paymentProviderServiceKafkaProperties
                    .getTopic("payment_provider_service_cancel_transaction_request");

            try {
                kafkaTemplate.send(topic, dto).get();
            } catch (ExecutionException | InterruptedException e) {
                logMessageOnError = String.format(
                        "Error occurred while sending cancel transaction request with transaction id: [%s] " +
                        "to kafka-topic: [%s] for service: [%s]",
                        transactionId, topic, paymentProviderServiceName);

                log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                        traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());

            } finally {
//               TODO: в будущем добавить логику асинхронной отправки сообщения в kafka для сервиса [webhook-service] для уведомления пользователя о результате обработки транзакции
            }
        } catch (NotFoundTopicException e) {
            onNotFoundTopicException(
                    e,
                    transactionId,
                    traceId,
                    spanId,
                    action
            );
        }
    }

    private void onNotFoundTopicException(
            NotFoundTopicException e,
            UUID transactionId,
            String traceId,
            String spanId,
            String action
    ) {
        String topic = e.getTopic();
        String logMessageOnError = String.format(
                "Error occurred while sending transaction with id: [%s] to kafka-topic: [%s]. " +
                "Topic with name: [%s] was not found.",
                transactionId, topic, topic);

        log.error("[{}-{}][{} -> {}]: {}. Exception: {}",
                traceId, spanId, serviceName, action, logMessageOnError, e.getMessage());
    }
}
