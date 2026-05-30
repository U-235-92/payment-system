package aq.project.services;

import aq.project.dto.ErrorDTO;
import aq.project.dto.OperationType;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.TransactionException;
import aq.project.messages.TransactionRequest;
import aq.project.messages.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static aq.project.util.RequestPropertyKeys.RECIPIENT_WALLET_ID;
import static aq.project.util.RequestPropertyKeys.SENDER_WALLET_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final static String BEARER = "Bearer ";

    public static final int MAX_NUMBER_OF_RETRIES = 3;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private final ScheduledExecutorService retryTransactionResponseScheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    @Value("${service.individuals-api.uri}")
    private String individualsApiUrl;
    @Value("${service.individuals-api.endpoints.handle-transaction-response}")
    private String individualsApiHandleTransactionResponseEndpoint;

    @Value("${service.wallet-service.uri}")
    private String walletServiceApiUrl;
    @Value("${service.wallet-service.endpoints.get-transaction-status}")
    private String walletServiceApiGetTransactionStatusEndpoint;

    @Value("${service.kafka.topics.wallet_operation_request.name}")
    private String walletOperationRequestTopicName;

    @Value("${service.kafka.partitions.deposit.name}")
    private String depositPartitionName;
    @Value("${service.kafka.partitions.withdraw.name}")
    private String withdrawPartitionName;
    @Value("${service.kafka.partitions.transfer.name}")
    private String transferPartitionName;

    private final RestClient restClient;

    private final TokenService tokenService;

    private final KafkaTemplate<String, TransactionRequest> kafkaTemplate;

    public String sendTransactionRequest(TransactionRequest transactionRequest) {
        sendTransactionRequest0(transactionRequest);
        return transactionRequest.getTransactionId();
    }

    private void sendTransactionRequest0(TransactionRequest transactionRequest) {
        if(transactionRequest.getOperationType() == OperationType.DEPOSIT) {
            ProducerRecord<String, TransactionRequest> record = new ProducerRecord<>(
                    walletOperationRequestTopicName,
                    depositPartitionName,
                    transactionRequest);
            Headers headers = record.headers();
            headers.add(RECIPIENT_WALLET_ID, getPropertyBytes(transactionRequest, RECIPIENT_WALLET_ID));
            kafkaTemplate.send(record);
        } else if(transactionRequest.getOperationType() == OperationType.WITHDRAW) {
            ProducerRecord<String, TransactionRequest> record = new ProducerRecord<>(
                    walletOperationRequestTopicName,
                    withdrawPartitionName,
                    transactionRequest);
            Headers headers = record.headers();
            headers.add(RECIPIENT_WALLET_ID, getPropertyBytes(transactionRequest, RECIPIENT_WALLET_ID));
            kafkaTemplate.send(record);
        } else if(transactionRequest.getOperationType() == OperationType.TRANSFER) {
            ProducerRecord<String, TransactionRequest> record = new ProducerRecord<>(
                    walletOperationRequestTopicName,
                    transferPartitionName,
                    transactionRequest);
            Headers headers = record.headers();
            headers.add(SENDER_WALLET_ID, getPropertyBytes(transactionRequest, SENDER_WALLET_ID));
            headers.add(RECIPIENT_WALLET_ID, getPropertyBytes(transactionRequest, RECIPIENT_WALLET_ID));
            kafkaTemplate.send(record);
        }
    }

    private byte[] getPropertyBytes(TransactionRequest transactionRequest, String key) {
        return transactionRequest.getProperty(key).getBytes();
    }

    @KafkaListener(topics = "${service.kafka.topics.wallet_operation_response.name}")
    public int handleTransactionResponse(TransactionResponse transactionResponse) {
        return handleTransactionResponse0(transactionResponse, new int[]{1});
    }

    private int handleTransactionResponse0(TransactionResponse transactionResponse, final int[] attempt) {
        String operation = transactionResponse.getOperationType().name().toLowerCase();
        return restClient.post()
                .uri(individualsApiUrl + individualsApiHandleTransactionResponseEndpoint)
                .body(transactionResponse)
                .header(HttpHeaders.AUTHORIZATION, BEARER + tokenService.getAdminAccessToken())
                .exchange((request, response) -> {
                    if(response.getStatusCode().is5xxServerError()) {
                        if(attempt[0] <= MAX_NUMBER_OF_RETRIES) {
                            log.warn(String.format("Handle %s transaction response failed. Unexpected individuals-api service exception occurred. Attempt %d of %d to handle transaction response with id [%s] again",
                                    operation, attempt[0], MAX_NUMBER_OF_RETRIES, transactionResponse.getTransactionId()));
                            attempt[0]++;
//                            Recursive async retry
                            retryTransactionResponseScheduler.schedule(
                                    () -> handleTransactionResponse0(transactionResponse, attempt),
                                    150L * attempt[0],
                                    TimeUnit.MILLISECONDS);
                        } else {
                            return response.getStatusCode().value();
                        }
                    }
                    return response.getStatusCode().value();
                });
    }

    public TransactionStatus getTransactionStatus(String transactionId) {
        return getTransactionStatus0(transactionId);
    }

    private TransactionStatus getTransactionStatus0(String transactionId) {
        String uri = walletServiceApiUrl + walletServiceApiGetTransactionStatusEndpoint + transactionId;
        String adminAccessToken = tokenService.getAdminAccessToken();
        return restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, BEARER + adminAccessToken)
                .exchange((request, response) -> {
                    if(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError()) {
                        ErrorDTO errorDto = response.bodyTo(ErrorDTO.class);
                        if(errorDto != null) {
                            throw new TransactionException(errorDto.getMessage());
                        }
                        throw new TransactionException(String.format("Unexpected error occurred during getting transaction status, transactionId: [%s] Status code: %d",
                                transactionId, response.getStatusCode().value()));
                    }
                    return response.bodyTo(TransactionStatus.class);
                });
    }
}
