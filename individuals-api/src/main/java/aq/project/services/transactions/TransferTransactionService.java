package aq.project.services.transactions;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.IndividualsApiServiceTransferTransactionRequestDto;
import aq.project.dto.TransactionServiceTransferTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.currency_rate.CurrencyRateService;
import aq.project.services.wallets.WalletService;
import aq.project.transaction_service.TransferTransactionApiClient;
import aq.project.utils.mappers.TransferTransactionMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.regex.Pattern;

import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static aq.project.utils.telemetry.TracePropagator.fetchTraceId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferTransactionService {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${application.individuals-api.endpoints.transfer-transaction-notification}")
    private String transferTransactionNotificationEndpoint;

    private final TransferTransactionMapper transferTransactionMapper = TransferTransactionMapper.INSTANCE;

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    private final CurrencyRateService currencyRateService;

    private final WalletService walletService;

    private final TransferTransactionApiClient transferTransactionApiClient;

    @RateLimiter(name = "create-transfer-transaction-rate-limiter", fallbackMethod = "createTransferTransactionFallback")
    @CircuitBreaker(name = "create-transfer-transaction-circuitbreaker", fallbackMethod = "createTransferTransactionFallback")
    public Mono<UUID> createTransferTransaction(
            IndividualsApiServiceTransferTransactionRequestDto transactionRequest
    ) {
        UUID requestSenderTransactionWalletId = transactionRequest.getSenderWalletId();
        UUID requestRecipientTransactionWalletId = transactionRequest.getRecipientWalletId();

        String requestTransactionCurrencyCode = transactionRequest.getCurrencyCode();

        BigDecimal requestTransactionAmount = transactionRequest.getAmount();

        if(!isValidAmount(requestTransactionAmount))
            return Mono.error(new ConstraintViolationException(
                    String.format(
                            "Error occurred during getting conversion rate while creating transfer transaction: " +
                            "received invalid transaction request amount: [%s]",
                            requestTransactionAmount),
                    null));

        return fetchTraceId()
                .flatMap(traceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> walletService.getWalletCurrencyCode(requestSenderTransactionWalletId)
                                .flatMap(senderWalletCurrencyCode -> walletService.getWalletCurrencyCode(requestRecipientTransactionWalletId)
                                        .flatMap(recipientWalletCurrencyCode -> getConversionRate(requestTransactionCurrencyCode, senderWalletCurrencyCode)
                                                .flatMap(senderConversionRate -> getConversionRate(requestTransactionCurrencyCode, recipientWalletCurrencyCode)
                                                        .flatMap(recipientConversionRate -> createTransferTransaction(traceId, jwtHeader, senderConversionRate, recipientConversionRate, transactionRequest)))))));
    }

    private Mono<UUID> createTransferTransactionFallback(
            IndividualsApiServiceTransferTransactionRequestDto transactionRequest,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.get(X_TRACE_ID_HEADER);
            String action = "create-transfer-transaction-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while creating transfer transaction. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    private boolean isValidAmount(BigDecimal amount) {
        return amount != null
                && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private Mono<BigDecimal> getConversionRate(
            String requestTransactionCurrencyCode,
            String walletServiceWalletCurrencyCode
    ) {
        if(!isValidCurrencyCode(requestTransactionCurrencyCode))
            return Mono.error(new ConstraintViolationException(
                    String.format(
                            "Error occurred during getting conversion rate while creating transfer transaction: " +
                            "received invalid transaction request currency code: [%s]",
                            requestTransactionCurrencyCode),
                    null));

        if(!isValidCurrencyCode(walletServiceWalletCurrencyCode))
            return Mono.error(new ConstraintViolationException(
                    String.format(
                            "Error occurred during getting conversion rate while creating transfer transaction: " +
                            "received invalid wallet currency code: [%s]",
                            walletServiceWalletCurrencyCode),
                    null));

        return currencyRateService
                .getRate(requestTransactionCurrencyCode, walletServiceWalletCurrencyCode, null, null)
                .flatMap(rateProviderServiceRateResponse -> Mono.just(rateProviderServiceRateResponse.getRate()));

    }

    private boolean isValidCurrencyCode(
            String currencyCode)
    {
        return currencyCode != null
                && Pattern.compile("^[A-Z]{3}$")
                .matcher(currencyCode)
                .matches();
    }

    private Mono<UUID> createTransferTransaction(
            String traceId,
            String authorization,
            BigDecimal senderConversionRate,
            BigDecimal recipientConversionRate,
            IndividualsApiServiceTransferTransactionRequestDto transactionRequest
    ) {
        if(authorization == null || authorization.isBlank())
            return Mono.error(new ConstraintViolationException(
                    "Error occurred while creating transfer transaction: received JWT is null or blank", null));

        if(traceId == null || traceId.isBlank())
            return Mono.error(new ConstraintViolationException(
                    "Error occurred while creating transfer transaction: received trace id is null or blank", null));

        if(!isValidConversionRate(senderConversionRate))
            return Mono.error(new ConstraintViolationException(
                    String.format(
                            "Error occurred while creating transfer transaction: " +
                            "received invalid sender conversion rate: [%s]",
                            senderConversionRate),
                    null));

        if(!isValidConversionRate(recipientConversionRate))
            return Mono.error(new ConstraintViolationException(
                    String.format(
                            "Error occurred while creating transfer transaction: " +
                            "received invalid recipient conversion rate: [%s]",
                            recipientConversionRate),
                    null));
        
        Mono<TransactionServiceTransferTransactionRequestDto> mono = getTransactionServiceTransferTransactionRequestDtoMono(
                transactionRequest, traceId, senderConversionRate, recipientConversionRate);

        return transferTransactionApiClient.createTransferTransaction(traceId, mono, authorization)
                .map(transactionServiceResponse -> transactionServiceResponse.getBody());
    }

    private boolean isValidConversionRate(
            BigDecimal conversionRate
    ) {
        return conversionRate != null
                && conversionRate.compareTo(BigDecimal.ZERO) > 0;
    }

    private Mono<TransactionServiceTransferTransactionRequestDto> getTransactionServiceTransferTransactionRequestDtoMono(
            IndividualsApiServiceTransferTransactionRequestDto transactionRequest,
            String traceId,
            BigDecimal senderConversionRate,
            BigDecimal recipientConversionRate
    ) {
        TransactionServiceTransferTransactionRequestDto dto = transferTransactionMapper.toTransactionServiceTransferTransactionRequestDto(transactionRequest);
        dto.setTraceId(traceId);
        dto.setSenderConversionRate(senderConversionRate);
        dto.setRecipientConversionRate(recipientConversionRate);
        dto.setNotificationUrl(transferTransactionNotificationEndpoint);
        return Mono.just(dto);
    }

    @RateLimiter(name = "get-transfer-transaction-status-rate-limiter", fallbackMethod = "getTransferTransactionStatusFallback")
    @CircuitBreaker(name = "create-transfer-transaction-circuitbreaker", fallbackMethod = "getTransferTransactionStatusFallback")
    public Mono<TransactionStatus> getTransferTransactionStatus(
            UUID transactionId
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> getTransferTransactionStatus(transactionId, xTraceId, jwtHeader)));
    }

    public Mono<TransactionStatus> getTransferTransactionStatusFallback(
            UUID transactionId,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.get(X_TRACE_ID_HEADER);
            String action = "get-transfer-transaction-status-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while getting transfer transaction status. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    private Mono<TransactionStatus> getTransferTransactionStatus(
            UUID transactionId,
            String traceId,
            String authorization
    ) {
        if(authorization == null || authorization.isBlank())
            return Mono.error(new ConstraintViolationException(
                    "Error occurred while creating transfer transaction: received JWT is null or blank", null));

        if(traceId == null || traceId.isBlank())
            return Mono.error(new ConstraintViolationException(
                    "Error occurred while creating transfer transaction: received trace id is null or blank", null));
        
        return transferTransactionApiClient.getTransferTransactionStatus(transactionId, traceId, authorization)
                .map(response -> response.getBody());
    }
}
