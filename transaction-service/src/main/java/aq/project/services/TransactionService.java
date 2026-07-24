package aq.project.services;

import aq.project.dto.TransactionStatus;
import aq.project.messages.TransactionRequest;
import aq.project.messages.TransactionResponse;
import aq.project.utils.telemetry.TraceContext;
import aq.project.wallet_service.TransactionApiClient;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;

@Service
@RequiredArgsConstructor
public class TransactionService {

    @Value("${service.kafka.topics.wallet_operation_request.name}")
    private String walletOperationRequestTopicName;

    @Value("${service.kafka.partitions.deposit.name}")
    private String depositPartitionName;
    @Value("${service.kafka.partitions.withdraw.name}")
    private String withdrawPartitionName;
    @Value("${service.kafka.partitions.transfer.name}")
    private String transferPartitionName;

    private final TokenService tokenService;

    private final KafkaTemplate<String, TransactionRequest> kafkaTemplate;

    private final TraceContext traceContext;

    private final TransactionApiClient transactionApiClient;

    public String sendTransactionRequest(TransactionRequest transactionRequest) {
        sendTransactionRequest0(transactionRequest);
        return transactionRequest.getTransactionId();
    }

    private void sendTransactionRequest0(TransactionRequest transactionRequest) {
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
            default -> throw new IllegalArgumentException(String.format("Unknown transaction operation type %s",
                    transactionRequest.getOperationType()));
        }
        Headers headers = record.headers();
        headers.add(X_TRACE_ID_HEADER, traceContext.getTraceId().getBytes());
        kafkaTemplate.send(record);
    }

    @KafkaListener(topics = "${service.kafka.topics.wallet_operation_response.name}")
    public void handleTransactionResponse(ConsumerRecord<String, TransactionResponse> consumerRecord) {
        TransactionResponse transactionResponse = consumerRecord.value();
    }

    public TransactionStatus getTransactionStatus(String transactionId) {
        String adminJwt = tokenService.getAdminJwtAsAuthorizationHeaderValue();
        String xTraceId = traceContext.getTraceId();
        return transactionApiClient.getTransactionStatus(transactionId, xTraceId, adminJwt).getBody();
    }
}
