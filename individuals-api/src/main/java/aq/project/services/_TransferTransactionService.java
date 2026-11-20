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
public class _TransferTransactionService {

    private final ConversionRateService conversionRateService;

    private final WalletService walletService;

    private final KeycloakServiceClientFacade keycloakClient;

    private final TransactionApiClient transactionClient;

    Mono<InitTransactionResponseDto> initTransaction(
            InitTransactionRequestDto initTransactionRequestDto
    ) {
        UUID senderWalletId = initTransactionRequestDto.getWalletId();
        UUID recipientWalletId = initTransactionRequestDto.getTargetWalletId();
        
        String requestCurrencyCode = initTransactionRequestDto.getCurrencyCode();

        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> walletService.getWalletCurrencyCode(senderWalletId)
                                .flatMap(senderCurrencyCode -> walletService.getWalletCurrencyCode(recipientWalletId)
                                        .flatMap(recipientCurrencyCode -> conversionRateService.getConversionRate(requestCurrencyCode, senderCurrencyCode)
                                                .flatMap(senderConversionRate -> conversionRateService.getConversionRate(requestCurrencyCode, recipientCurrencyCode)
                                                        .flatMap(recipientConversionRate -> initTransaction(traceId, authorization, senderConversionRate, recipientConversionRate, initTransactionRequestDto)))))));
    }

    private Mono<InitTransactionResponseDto> initTransaction(
            String traceId,
            String authorization,
            BigDecimal walletConversionRate,
            BigDecimal targetWalletConversionRate,
            InitTransactionRequestDto initTransactionRequestDto
    ) {
        if(authorization == null || authorization.isBlank()) {
            String msg = "Error occurred while transfer transaction was initialized: received JWT is null or blank";

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(traceId == null || traceId.isBlank()) {
            String msg = "Error occurred while transfer transaction was initialized: received trace id is null or blank";

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(!conversionRateService.isValidConversionRate(walletConversionRate)) {
            String msg = String.format("Error occurred while transfer transaction was initialized: " +
                    "received invalid wallet conversion rate: [%s]", walletConversionRate);

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(!conversionRateService.isValidConversionRate(targetWalletConversionRate)) {
            String msg = String.format("Error occurred while transfer transaction was initialized: " +
                    "received invalid target wallet conversion rate: [%s]", targetWalletConversionRate);

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        initTransactionRequestDto.setWalletConversionRate(walletConversionRate);
        initTransactionRequestDto.setTargetWalletConversionRate(targetWalletConversionRate);

        return transactionClient
                .initTransaction(PaymentType.TRANSFER.toString(), Mono.just(initTransactionRequestDto), traceId, authorization)
                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())));
    }

    Mono<UUID> confirmTransaction(
            ConfirmTransactionRequestDto confirmTransactionRequestDto
    ) {
        UUID senderWalletId = confirmTransactionRequestDto.getWalletId();
        UUID recipientWalletId = confirmTransactionRequestDto.getTargetWalletId();

        String requestCurrencyCode = confirmTransactionRequestDto.getCurrencyCode();

        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> walletService.getWalletCurrencyCode(senderWalletId)
                                .flatMap(senderCurrencyCode -> walletService.getWalletCurrencyCode(recipientWalletId)
                                        .flatMap(recipientCurrencyCode -> conversionRateService.getConversionRate(requestCurrencyCode, senderCurrencyCode)
                                                .flatMap(senderConversionRate -> conversionRateService.getConversionRate(requestCurrencyCode, recipientCurrencyCode)
                                                        .flatMap(recipientConversionRate -> confirmTransaction(traceId, authorization, senderConversionRate, recipientConversionRate, confirmTransactionRequestDto)))))));
    }

    private Mono<UUID> confirmTransaction(
            String traceId,
            String authorization,
            BigDecimal walletConversionRate,
            BigDecimal targetWalletConversionRate,
            ConfirmTransactionRequestDto confirmTransactionRequestDto
    ) {
        if(authorization == null || authorization.isBlank()) {
            String msg = "Error occurred while transfer transaction was confirmed: received JWT is null or blank";

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(traceId == null || traceId.isBlank()) {
            String msg = "Error occurred while transfer transaction was confirmed: received trace id is null or blank";

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(!conversionRateService.isValidConversionRate(walletConversionRate)) {
            String msg = String.format("Error occurred while transfer transaction was initialized: " +
                    "received invalid wallet conversion rate: [%s]", walletConversionRate);

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        if(!conversionRateService.isValidConversionRate(targetWalletConversionRate)) {
            String msg = String.format("Error occurred while transfer transaction was initialized: " +
                    "received invalid target wallet conversion rate: [%s]", targetWalletConversionRate);

            return Mono.error(new ConstraintViolationException(msg, null));
        }

        confirmTransactionRequestDto.setWalletConversionRate(walletConversionRate);
        confirmTransactionRequestDto.setTargetWalletConversionRate(targetWalletConversionRate);

        return transactionClient.confirmTransaction(PaymentType.TRANSFER.toString(), Mono.just(confirmTransactionRequestDto), traceId, authorization)
                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())));
    }
}