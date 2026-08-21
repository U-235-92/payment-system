package aq.project.utils.handlers;

import aq.project.dto.CancelTransactionDto;
import aq.project.dto.TransactionStatus;
import aq.project.dto.TransactionStatusDto;
import aq.project.entities.Transaction;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ExceedAttemptLimitException;
import aq.project.messages.TransactionRequest;
import aq.project.messages.TransactionResponse;
import aq.project.payment_provider_service.WebhookApiClient;
import aq.project.repositories.TransactionRepository;
import aq.project.utils.mappers.TransactionRequestMapper;
import aq.project.utils.mappers.TransactionResponseMapper;
import aq.project.utils.security.BasicAuthorizationHeaderGenerator;
import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;

@Component
@RequiredArgsConstructor
public class MessageBrokerHandler {

    @Value("${service.kafka.topics.wallet_operation_request.name}")
    private String walletOperationRequestTopicName;

    @Value("${service.transaction-service.retry.send-transaction-request-to-message-broker.max-attempts}")
    private int maxNumberAttemptsToSendTransactionRequestToMessageBroker;

    @Value("${service.kafka.partitions.deposit.name}")
    private String depositPartitionName;
    @Value("${service.kafka.partitions.withdraw.name}")
    private String withdrawPartitionName;
    @Value("${service.kafka.partitions.transfer.name}")
    private String transferPartitionName;

    @Value("${service.transaction-service.merchant-id}")
    private String merchantId;
    @Value("${service.transaction-service.merchant-secret}")
    private String merchantSecret;

    private final TransactionResponseMapper transactionResponseMapper = TransactionResponseMapper.INSTANCE;
    private final TransactionRequestMapper transactionRequestMapper = TransactionRequestMapper.INSTANCE;

    private final KafkaTemplate<String, TransactionRequest> kafkaTemplate;

    private final TraceContext traceContext;

    private final WebhookApiClient paymentServiceWebhookApiClient;

    private final PaymentServiceHandler paymentServiceHandler;

    private final TransactionRepository transactionRepository;

    public void sendTransactionRequestToMessageBroker(
            Transaction transaction
    ) throws ExecutionException, InterruptedException {
        if(transaction.getMetadata().getRetryCount() >= maxNumberAttemptsToSendTransactionRequestToMessageBroker)
            throw new ExceedAttemptLimitException(String.format(
                    "Error occurred while trying to send transaction with id: [%s] to message broker", transaction));

        TransactionRequest transactionRequest = transactionRequestMapper.toTransactionRequest(transaction);
        ProducerRecord<String, TransactionRequest> record;
        switch (transactionRequest.getOperationType()) {
            case DEPOSIT -> record = new ProducerRecord<>(
                    walletOperationRequestTopicName,
                    depositPartitionName,
                    transactionRequest
            );
            case WITHDRAW -> record = new ProducerRecord<>(
                    walletOperationRequestTopicName,
                    withdrawPartitionName,
                    transactionRequest
            );
            case TRANSFER -> record = new ProducerRecord<>(
                    walletOperationRequestTopicName,
                    transferPartitionName,
                    transactionRequest
            );
            default -> throw new IllegalArgumentException(
                    String.format("Unknown transaction operation type %s", transactionRequest.getOperationType()));
        }
        Headers headers = record.headers();
        headers.add(X_TRACE_ID_HEADER, traceContext.getTraceId().getBytes());
        kafkaTemplate.send(record).get();
    }

    @KafkaListener(
            topics = "${service.kafka.topics.wallet_operation_response.name}"
    )
    public void processTransactionResponse(
            ConsumerRecord<String, TransactionResponse> consumerRecord
    ) {
        TransactionResponse transactionResponse = consumerRecord.value();
        try {
            String transactionId = transactionResponse.getTransactionId();

            Transaction transaction = getTransaction(transactionId);

            String xTraceId = transaction.getMetadata().getTraceId();

            traceContext.clean();
            traceContext.setTraceId(xTraceId);

            if(!transaction.isProcessed()) {
                TransactionStatus transactionStatus = transactionResponse.getTransactionStatus();
                if(transactionStatus == TransactionStatus.FAILED) {
                    sendCancelTransactionRequestToPaymentService(transactionResponse);
                } else {
                    updateTransactionStatusOnPaymentService(transactionResponse);
                }
                transaction.setStatus(transactionStatus);
                transaction.setProcessed(true);
                transactionRepository.save(transaction);
            }
        } finally {
            traceContext.clean();
        }
    }

    private Transaction getTransaction(String transactionId) {
        String errorMessage = String.format(
                "Error occurred while handle transaction response transaction with id: [%s] wasn't found",
                transactionId);

        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException(errorMessage));
    }

    private void sendCancelTransactionRequestToPaymentService(
            TransactionResponse transactionResponse
    ) {
        CancelTransactionDto cancelTransactionDto = new CancelTransactionDto();
        cancelTransactionDto.setId(transactionResponse.getTransactionId());
        cancelTransactionDto.setStatus(transactionResponse.getTransactionStatus());

        paymentServiceHandler.sendCancelTransactionRequestToPaymentService(cancelTransactionDto);
    }

    private void updateTransactionStatusOnPaymentService(TransactionResponse transactionResponse) {
        TransactionStatusDto transactionStatusDto = transactionResponseMapper
                .toTransactionStatusDto(transactionResponse);

        String xTraceId = traceContext.getTraceId();

        String basicAuthorizationHeaderValue = BasicAuthorizationHeaderGenerator
                .getBase64BasicAuthorizationValue(merchantId, merchantSecret);

        paymentServiceWebhookApiClient
                .updateTransactionStatus(basicAuthorizationHeaderValue, transactionStatusDto, xTraceId);
    }
}
