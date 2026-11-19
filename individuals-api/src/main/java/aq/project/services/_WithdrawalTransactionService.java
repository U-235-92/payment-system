package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.ConfirmTransactionRequestDto;
import aq.project.dto.InitTransactionRequestDto;
import aq.project.dto.InitTransactionResponseDto;
import aq.project.dto.PaymentType;
import aq.project.transaction_service.TransactionApiClient;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static aq.project.telemetry.TracePropagator.fetchTraceId;

@Service
@RequiredArgsConstructor
public class _WithdrawalTransactionService {

    private final ConversionRateService conversionRateService;

    private final WalletService walletService;

    private final KeycloakServiceClientFacade keycloakClient;

    private final TransactionApiClient transactionClient;

    Mono<InitTransactionResponseDto> initTransaction(
            InitTransactionRequestDto initTransactionRequestDto
    ) {
        UUID requestWalletId = initTransactionRequestDto.getWalletId();

        String requestCurrencyCode = initTransactionRequestDto.getCurrencyCode();

        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> walletService.getWalletCurrencyCode(requestWalletId)
                                .flatMap(walletCurrencyCode -> conversionRateService.getConversionRate(requestCurrencyCode, walletCurrencyCode)
                                        .flatMap(conversionRate -> initTransaction(traceId, authorization, conversionRate, initTransactionRequestDto)))));
    }

    private Mono<InitTransactionResponseDto> initTransaction(
            String traceId,
            String authorization,
            BigDecimal conversionRate,
            InitTransactionRequestDto initTransactionRequestDto
    ) {
        if(authorization == null || authorization.isBlank()) {
            String msg = "Error occurred while withdrawal transaction was initialized: received JWT is null or blank";

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(traceId == null || traceId.isBlank()) {
            String msg = "Error occurred while withdrawal transaction was initialized: received trace id is null or blank";

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(!conversionRateService.isValidConversionRate(conversionRate)) {
            String msg = String.format("Error occurred while withdrawal transaction was initialized: " +
                    "received invalid conversion rate: [%s]", conversionRate);

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        initTransactionRequestDto.setWalletConversionRate(conversionRate);

        return transactionClient
                .initTransaction(PaymentType.WITHDRAWAL, Mono.just(initTransactionRequestDto), traceId, authorization)
                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())));
    }

    Mono<UUID> confirmTransaction(
            ConfirmTransactionRequestDto confirmTransactionRequestDto
    ) {
        UUID requestWalletId = confirmTransactionRequestDto.getWalletId();

        String requestCurrencyCode = confirmTransactionRequestDto.getCurrencyCode();

        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> walletService.getWalletCurrencyCode(requestWalletId)
                                .flatMap(walletCurrencyCode -> conversionRateService.getConversionRate(requestCurrencyCode, walletCurrencyCode)
                                        .flatMap(conversionRate -> confirmTransaction(traceId, authorization, conversionRate, confirmTransactionRequestDto)))));
    }

    private Mono<UUID> confirmTransaction(
            String traceId,
            String authorization,
            BigDecimal conversionRate,
            ConfirmTransactionRequestDto confirmTransactionRequestDto
    ) {
        if(authorization == null || authorization.isBlank()) {
            String msg = "Error occurred while withdrawal transaction was confirmed: received JWT is null or blank";

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(traceId == null || traceId.isBlank()) {
            String msg = "Error occurred while withdrawal transaction was confirmed: received trace id is null or blank";

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(!conversionRateService.isValidConversionRate(conversionRate)) {
            String msg = String.format("Error occurred while withdrawal transaction was confirmed: " +
                    "received invalid conversion rate: [%s]", conversionRate);

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        confirmTransactionRequestDto.setWalletConversionRate(conversionRate);

        return transactionClient.confirmTransaction(PaymentType.WITHDRAWAL, Mono.just(confirmTransactionRequestDto), traceId, authorization)
                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())));
    }
}