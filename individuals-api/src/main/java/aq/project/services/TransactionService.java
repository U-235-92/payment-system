package aq.project.services;

import aq.project.clients.KeycloakServiceWebClientFacade;
import aq.project.clients.TransactionServiceWebClient;
import aq.project.dto.OperationType;
import aq.project.dto.TransactionRequestDTO;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.TransactionException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static aq.project.util.constants.RequestPropertyKeys.*;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionServiceWebClient transactionServiceWebClient;

    private final KeycloakServiceWebClientFacade keycloakServiceWebClientFacade;

    private final WalletService walletService;

    private final CurrencyRateService currencyRateService;

    public Mono<TransactionStatus> getTransactionStatus(String transactionId) {
        return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                .flatMap(jwt -> transactionServiceWebClient.getTransactionStatus(jwt, transactionId)
                        .flatMap(response -> getTransactionStatus(transactionId, response)));
    }

    private Mono<TransactionStatus> getTransactionStatus(
            String transactionId,
            ResponseEntity<TransactionStatus> response
    ) {
        if(isErrorResponse(response)) {
            String cause = "Error occurred during getting transaction status with transactionId:";
            String msg = String.format("%s [%s]", cause, transactionId);
            int status = response.getStatusCode().value();
            return Mono.error(new TransactionException(msg, status));
        }
        return Mono.just(response.getBody());
    }

    public Mono<String> doTransaction(TransactionRequestDTO transactionRequestDTO) {
        return Mono.just(transactionRequestDTO).flatMap(dto -> {
            dto.setTimestamp(System.currentTimeMillis());
            return switch (dto.getOperationType()) {
                case OperationType.WITHDRAW, OperationType.DEPOSIT -> walletService
                        .getWalletCurrencyCode(dto.getProperties().get(RECIPIENT_WALLET_ID))
                        .flatMap(walletCurrencyCode -> getRate(dto.getCurrency(), walletCurrencyCode, dto.getRateProvider(), dto.getCurrencyRateDate()))
                        .flatMap(rate -> {
                            dto.getProperties().put(RECIPIENT_CURRENCY_RATE, rate);
                            return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                                    .flatMap(jwt -> transactionServiceWebClient.doTransaction(jwt, dto)
                                            .flatMap(response -> onDoTransactionResponse(response, transactionRequestDTO)));
                        });
                case OperationType.TRANSFER -> walletService
                        .getWalletCurrencyCode(dto.getProperties().get(RECIPIENT_WALLET_ID))
                        .flatMap(recipientCurrencyCode -> walletService
                                .getWalletCurrencyCode(dto.getProperties().get(SENDER_WALLET_ID))
                                .flatMap(senderCurrencyCode -> getRate(dto.getCurrency(), recipientCurrencyCode, dto.getRateProvider(), dto.getCurrencyRateDate())
                                        .flatMap(recipientCurrencyRate -> getRate(dto.getCurrency(), senderCurrencyCode, dto.getRateProvider(), dto.getCurrencyRateDate())
                                                .flatMap(senderCurrencyRate -> {
                                                    dto.getProperties().put(RECIPIENT_CURRENCY_RATE, recipientCurrencyRate);
                                                    dto.getProperties().put(SENDER_CURRENCY_RATE, senderCurrencyRate);
                                                    return keycloakServiceWebClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                                                            .flatMap(jwt -> transactionServiceWebClient.doTransaction(jwt, dto)
                                                                    .flatMap(response -> onDoTransactionResponse(response, transactionRequestDTO)));
                                                })
                                        )
                                )
                        );
            };
        });
    }

    private Mono<String> onDoTransactionResponse(ResponseEntity<String> response, TransactionRequestDTO dto) {
        if(isErrorResponse(response)) {
            String msg = String.format("Error occurred during doing %s transaction",
                    dto.getOperationType().name().toLowerCase());
            if(dto.getOperationType() == OperationType.DEPOSIT || dto.getOperationType() == OperationType.WITHDRAW)
                msg += String.format(", for wallet with id [%s]",
                        dto.getProperties().get(RECIPIENT_WALLET_ID));
            else if(dto.getOperationType() == OperationType.TRANSFER)
                msg += String.format(", for sender's wallet with id [%s] and recipient's wallet with id [%s].",
                        dto.getProperties().get(SENDER_WALLET_ID), dto.getProperties().get(RECIPIENT_WALLET_ID));
            throw new TransactionException(msg, response.getStatusCode().value());
        }
        return Mono.just(response.getBody());
    }

    private boolean isErrorResponse(ResponseEntity<?> response) {
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    private Mono<String> getRate(String from, String to, String provider, LocalDate date) {
        return currencyRateService.getRate(from, to, provider, date)
                .map(rateResponse -> {
                    if(rateResponse.getRate() == null)
                        throw new IllegalStateException(String.format("Error occurred during getting rate of [%s -> %s]",
                                from, to));
                    return String.valueOf(rateResponse.getRate());
                });
    }
}
