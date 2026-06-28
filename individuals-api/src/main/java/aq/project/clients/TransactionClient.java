package aq.project.clients;

import aq.project.dto.OperationType;
import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static aq.project.util.constants.CustomHttpHeaders.*;
import static aq.project.util.constants.RequestPropertyKeys.RECIPIENT_WALLET_ID;
import static aq.project.util.constants.RequestPropertyKeys.SENDER_WALLET_ID;

@Component
public class TransactionClient {

    @Value("${application.transaction-service.endpoints.get-transaction-status}")
    private String getTransactionStausUri;
    @Value("${application.transaction-service.endpoints.send-transaction-request}")
    private String sendTransactionRequestUri;

    @Autowired
    private JwtClient jwtClient;

    @Autowired
    @Qualifier("transactionServiceWebClient")
    private WebClient webClient;

    public Mono<TransactionStatus> getTransactionStatus(String transactionId) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> getTransactionStatus(transactionId, adminAccessToken));
    }

    private Mono<TransactionStatus> getTransactionStatus(String transactionId, String accessToken) {
        return webClient.get()
                .uri(getTransactionStausUri + transactionId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
                .exchangeToMono(response -> {
                    if(response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                        String msg = String.format("Error occurred during getting transaction status with transactionId: [%s]",
                                transactionId);
                        return Mono.error(new TransactionException(msg, response.statusCode().value()));
                    }
                    return response.bodyToMono(TransactionStatus.class);
                });
    }

    public Mono<String> doTransaction(TransactionRequestDTO dto) {
        return jwtClient.requestAdminToken()
                .flatMap(adminAccessToken -> doTransaction(dto, adminAccessToken));
    }

    private Mono<String> doTransaction(TransactionRequestDTO dto, String accessToken) {
        return webClient.post()
                .uri(sendTransactionRequestUri)
                .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
                .bodyValue(dto)
                .exchangeToMono(response -> {
                    if(response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                        String msg = String.format("Error occurred during doing %s transaction",
                                dto.getOperationType().name().toLowerCase());
                        if(dto.getOperationType() == OperationType.DEPOSIT || dto.getOperationType() == OperationType.WITHDRAW)
                            msg += String.format(", for wallet with id [%s]",
                                    dto.getProperties().get(RECIPIENT_WALLET_ID));
                        else if(dto.getOperationType() == OperationType.TRANSFER)
                            msg += String.format(", for sender's wallet with id [%s] and recipient's wallet with id [%s].",
                                    dto.getProperties().get(SENDER_WALLET_ID), dto.getProperties().get(RECIPIENT_WALLET_ID));
                        return Mono.error(new TransactionException(msg, response.statusCode().value()));
                    }
                    return response.bodyToMono(String.class);
                });
    }
}
