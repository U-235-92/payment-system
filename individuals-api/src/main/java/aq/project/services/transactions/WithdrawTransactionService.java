package aq.project.services.transactions;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.IndividualsApiServiceWithdrawTransactionRequestDto;
import aq.project.dto.TransactionServiceWithdrawTransactionRequestDto;
import aq.project.dto.TransactionStatus;
import aq.project.exceptions.FallbackOperationException;
import aq.project.services.currency_rate.CurrencyRateService;
import aq.project.services.wallets.WalletService;
import aq.project.transaction_service.WithdrawTransactionApiClient;
import aq.project.utils.mappers.WithdrawTransactionMapper;
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
public class WithdrawTransactionService {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${application.individuals-api.endpoints.withdraw-transaction-notification}")
    private String withdrawTransactionNotificationEndpoint;

    private final WithdrawTransactionMapper withdrawTransactionMapper = WithdrawTransactionMapper.INSTANCE;

    private final KeycloakServiceClientFacade keycloakServiceClientFacade;

    private final CurrencyRateService currencyRateService;

    private final WalletService walletService;

    private final WithdrawTransactionApiClient withdrawTransactionApiClient;

    @RateLimiter(name = "create-withdraw-transaction-rate-limiter", fallbackMethod = "createWithdrawTransactionFallback")
    @CircuitBreaker(name = "create-withdraw-transaction-circuitbreaker", fallbackMethod = "createWithdrawTransactionFallback")
    public Mono<UUID> createWithdrawTransaction(
            IndividualsApiServiceWithdrawTransactionRequestDto transactionRequest
    ) {
        UUID requestTransactionWalletId = transactionRequest.getWalletId();

        String requestTransactionCurrencyCode = transactionRequest.getCurrencyCode();

        BigDecimal requestTransactionAmount = transactionRequest.getAmount();

        if(!isValidAmount(requestTransactionAmount))
            return Mono.error(new ConstraintViolationException(
                    String.format(
                            "Error occurred during getting conversion rate while creating withdraw transaction: " +
                            "received invalid transaction request amount: [%s]",
                            requestTransactionAmount),
                    null));

        return fetchTraceId()
                .flatMap(traceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> walletService.getWalletCurrencyCode(requestTransactionWalletId)
                                .flatMap(walletServiceWalletCurrencyCode -> getConversionRate(requestTransactionCurrencyCode, walletServiceWalletCurrencyCode))
                                    .flatMap(conversionRate -> createWithdrawTransaction(traceId, jwtHeader, conversionRate, transactionRequest))));
    }

    private Mono<UUID> createWithdrawTransactionFallback(
            IndividualsApiServiceWithdrawTransactionRequestDto transactionRequest,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.get(X_TRACE_ID_HEADER);
            String action = "create-withdraw-transaction-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while creating withdraw transaction. " +
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
                            "Error occurred during getting conversion rate while creating withdraw transaction: " +
                            "received invalid transaction request currency code: [%s]",
                            requestTransactionCurrencyCode),
                    null));

        if(!isValidCurrencyCode(walletServiceWalletCurrencyCode))
            return Mono.error(new ConstraintViolationException(
                    String.format(
                            "Error occurred during getting conversion rate while creating withdraw transaction: " +
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

    private Mono<UUID> createWithdrawTransaction(
            String traceId,
            String authorization,
            BigDecimal conversionRate,
            IndividualsApiServiceWithdrawTransactionRequestDto transactionRequest
    ) {
        if(authorization == null || authorization.isBlank())
            return Mono.error(new ConstraintViolationException(
                    "Error occurred while creating withdraw transaction: received JWT is null or blank", null));

        if(traceId == null || traceId.isBlank())
            return Mono.error(new ConstraintViolationException(
                    "Error occurred while creating withdraw transaction: received trace id is null or blank", null));

        if(!isValidConversionRate(conversionRate))
            return Mono.error(new ConstraintViolationException(
                    String.format(
                            "Error occurred while creating withdraw transaction: " +
                            "received invalid conversion rate: [%s]",
                            conversionRate),
                    null));
        
        Mono<TransactionServiceWithdrawTransactionRequestDto> mono = getTransactionServiceWithdrawTransactionRequestDtoMono(
                transactionRequest, traceId, conversionRate);

        return withdrawTransactionApiClient.createWithdrawTransaction(traceId, mono, authorization)
                .map(transactionServiceResponse -> transactionServiceResponse.getBody());
    }

    private boolean isValidConversionRate(
            BigDecimal conversionRate
    ) {
        return conversionRate != null
                && conversionRate.compareTo(BigDecimal.ZERO) > 0;
    }

    private Mono<TransactionServiceWithdrawTransactionRequestDto> getTransactionServiceWithdrawTransactionRequestDtoMono(
            IndividualsApiServiceWithdrawTransactionRequestDto transactionRequest,
            String traceId,
            BigDecimal conversionRate
    ) {
        TransactionServiceWithdrawTransactionRequestDto dto = withdrawTransactionMapper.toTransactionServiceWithdrawTransactionRequestDto(transactionRequest);

        dto.setTraceId(traceId);
        dto.setConversionRate(conversionRate);
        dto.setNotificationUrl(withdrawTransactionNotificationEndpoint);

        return Mono.just(dto);
    }

    @RateLimiter(name = "get-withdraw-transaction-status-rate-limiter", fallbackMethod = "getWithdrawTransactionStatusFallback")
    @CircuitBreaker(name = "create-withdraw-transaction-circuitbreaker", fallbackMethod = "getWithdrawTransactionStatusFallback")
    public Mono<TransactionStatus> getWithdrawTransactionStatus(
            UUID transactionId
    ) {
        return fetchTraceId()
                .flatMap(xTraceId -> keycloakServiceClientFacade.getAdminJwtAsAuthorizationHeaderValue()
                        .flatMap(jwtHeader -> getWithdrawTransactionStatus(transactionId, xTraceId, jwtHeader) ));
    }

    public Mono<TransactionStatus> getWithdrawTransactionStatusFallback(
            UUID transactionId,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.get(X_TRACE_ID_HEADER);
            String action = "get-withdraw-transaction-status-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while getting withdraw transaction status. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    private Mono<TransactionStatus> getWithdrawTransactionStatus(
            UUID transactionId,
            String traceId,
            String authorization
    ) {
        if(authorization == null || authorization.isBlank())
            return Mono.error(new ConstraintViolationException(
                    "Error occurred while creating withdraw transaction: received JWT is null or blank", null));

        if(traceId == null || traceId.isBlank())
            return Mono.error(new ConstraintViolationException(
                    "Error occurred while creating withdraw transaction: received trace id is null or blank", null));
        
        return withdrawTransactionApiClient.getWithdrawTransactionStatus(transactionId, traceId, authorization)
                .map(response -> response.getBody());
    }
}
