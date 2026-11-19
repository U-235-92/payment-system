package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.utils.telemetry.TraceFactory;
import aq.project.wallet_service.WalletApiClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

import static aq.project.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static aq.project.telemetry.TracePropagator.fetchTraceId;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    @Value("${spring.application.name}")
    private String serviceName;

    private final WalletApiClient walletClient;

    private final KeycloakServiceClientFacade keycloakClient;

    @RateLimiter(
            name = "create-wallet-rate-limiter",
            fallbackMethod = "createWalletFallback"
    )
    @CircuitBreaker(
            name = "create-wallet-circuitbreaker",
            fallbackMethod = "createWalletFallback"
    )
    public Mono<UUID> createWallet(
            CreateWalletRequestDto createWalletRequestDto
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(jwtHeader -> walletClient.createWallet(Mono.just(createWalletRequestDto), traceId, jwtHeader)))
                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())));
    }

    private Mono<UUID> createWalletFallback(
            CreateWalletRequestDto request,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
            String action = "create-wallet-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while creating a new wallet. Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    @RateLimiter(
            name = "get-wallet-info-rate-limiter",
            fallbackMethod = "getWalletInfoFallback"
    )
    @CircuitBreaker(
            name = "get-wallet-info-circuitbreaker",
            fallbackMethod = "getWalletInfoFallback"
    )
    public Mono<WalletInfoResponseDto> getWalletInfo(
            UUID walletId
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> walletClient.getWalletInfo(walletId, traceId, authorization)))
                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())));
    }

    private Mono<WalletInfoResponseDto> getWalletInfoFallback(
            UUID walletId,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
            String action = "get-wallet-info-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while getting wallet info. Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    @RateLimiter(
            name = "get-wallet-currency-code-rate-limiter",
            fallbackMethod = "getWalletCurrencyCodeFallback"
    )
    @CircuitBreaker(
            name = "get-wallet-currency-code-circuitbreaker",
            fallbackMethod = "getWalletCurrencyCodeFallback"
    )
    public Mono<String> getWalletCurrencyCode(
            UUID walletId
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> walletClient.getWalletCurrency(walletId, traceId, authorization)))
                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())));
    }

    private Mono<String> getWalletCurrencyCode(
            UUID walletId,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
            String action = "get-wallet-currency-code-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while getting currency code of wallet. Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }
}