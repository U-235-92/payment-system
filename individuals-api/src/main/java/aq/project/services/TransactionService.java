package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.OperationType;
import aq.project.dto.TransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.TransactionException;
import aq.project.transaction_service.TransactionApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static aq.project.dto.OperationType.*;
import static aq.project.utils.constants.RequestPropertyKeys.*;
import static aq.project.utils.telemetry.TracePropagator.fetchTraceId;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionApiClient transactionApiClient;

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    private final WalletService walletService;

    private final CurrencyRateService currencyRateService;

    public Mono<TransactionStatus> getTransactionStatus(
            String transactionId
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> transactionApiClient
                                .getTransactionStatus(transactionId, xTraceId, jwtHeader)
                                    .flatMap(response ->
                                            getTransactionStatus(transactionId, response))
                        )
                );
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

    public Mono<String> doTransaction(
            TransactionRequestDto dto
    ) {
        dto.setTimestamp(System.currentTimeMillis());
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> switch (dto.getOperationType()) {
                            case WITHDRAW, DEPOSIT -> walletService
                                    .getWalletCurrencyCode(dto.getProperties().get(RECIPIENT_WALLET_ID))
                                    .flatMap(currencyCode -> getRate(dto.getCurrency(), currencyCode, dto.getRateProvider(), dto.getCurrencyRateDate()))
                                    .flatMap(rate -> {
                                        dto.getProperties().put(RECIPIENT_CURRENCY_RATE, rate);
                                        return transactionApiClient.sendTransactionRequest(xTraceId, Mono.just(dto), jwtHeader);
                                    })
                                    .flatMap(response -> onDoTransactionResponse(response, dto));
                            case TRANSFER -> walletService
                                    .getWalletCurrencyCode(dto.getProperties().get(RECIPIENT_WALLET_ID))
                                    .flatMap(recipientCurrencyCode -> walletService
                                            .getWalletCurrencyCode(dto.getProperties().get(SENDER_WALLET_ID))
                                            .flatMap(senderCurrencyCode -> getRate(dto.getCurrency(), recipientCurrencyCode, dto.getRateProvider(), dto.getCurrencyRateDate())
                                                    .flatMap(recipientCurrencyRate -> getRate(dto.getCurrency(), senderCurrencyCode, dto.getRateProvider(), dto.getCurrencyRateDate())
                                                            .flatMap(senderCurrencyRate -> {
                                                                dto.getProperties().put(RECIPIENT_CURRENCY_RATE, recipientCurrencyRate);
                                                                dto.getProperties().put(SENDER_CURRENCY_RATE, senderCurrencyRate);
                                                                return transactionApiClient.sendTransactionRequest(xTraceId, Mono.just(dto), jwtHeader);
                                                            })
                                                            .flatMap(response -> onDoTransactionResponse(response, dto))
                                                    )
                                            )
                                    );

                        }));
    }

    private Mono<String> onDoTransactionResponse(
            ResponseEntity<String> response,
            TransactionRequestDto dto
    ) {
        if(isErrorResponse(response)) {
            String msg = String.format("Error occurred during doing %s transaction",
                    dto.getOperationType().name().toLowerCase());
            if(isEqualOperationType(dto, DEPOSIT) || isEqualOperationType(dto, WITHDRAW))
                msg += String.format(", for wallet with id [%s]",
                        dto.getProperties().get(RECIPIENT_WALLET_ID));
            else if(isEqualOperationType(dto, TRANSFER))
                msg += String.format(", for sender's wallet with id [%s] and recipient's wallet with id [%s].",
                        dto.getProperties().get(SENDER_WALLET_ID), dto.getProperties().get(RECIPIENT_WALLET_ID));
            throw new TransactionException(msg, response.getStatusCode().value());
        }
        return Mono.just(response.getBody());
    }

    private boolean isEqualOperationType(
            TransactionRequestDto dto,
            OperationType operationType
    ) {
        return dto.getOperationType().getValue().equals(operationType.getValue());
    }

    private boolean isErrorResponse(
            ResponseEntity<?> response
    ) {
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    private Mono<String> getRate(
            String from,
            String to,
            String provider,
            OffsetDateTime date
    ) {
        return currencyRateService.getRate(from, to, provider, date)
                .map(rateResponse -> {
                    if(rateResponse.getRate() == null)
                        throw new IllegalStateException(String.format("Error occurred during getting rate of [%s -> %s]",
                                from, to));
                    return String.valueOf(rateResponse.getRate());
                });
    }
}
