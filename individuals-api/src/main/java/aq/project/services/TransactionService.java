package aq.project.services;

import aq.project.clients.KeycloakServiceClientFacade;
import aq.project.dto.*;
import aq.project.exceptions.FallbackOperationException;
import aq.project.transaction_service.TransactionApiClient;
import aq.project.utils.telemetry.TraceFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import static aq.project.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;
import static aq.project.telemetry.TracePropagator.fetchTraceId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    @Value("${spring.application.name}")
    private String serviceName;

    private final _DepositTransactionService depositTransactionService;

    private final _WithdrawalTransactionService withdrawalTransactionService;

    private final _TransferTransactionService transferTransactionService;

    private final KeycloakServiceClientFacade keycloakClient;

    private final TransactionApiClient transactionClient;

    @RateLimiter(
            name = "init-transaction-rate-limiter",
            fallbackMethod = "initTransactionFallback"
    )
    @CircuitBreaker(
            name = "init-transaction-circuitbreaker",
            fallbackMethod = "initTransactionFallback"
    )
    public Mono<InitTransactionResponseDto> initTransaction(
            PaymentType type,
            InitTransactionRequestDto initTransactionRequestDto
    ) {
        switch(type) {
            case DEPOSIT -> {
                initTransactionRequestDto.setPaymentType(type);

                return depositTransactionService.initTransaction(initTransactionRequestDto);
            }

            case WITHDRAWAL -> {
                initTransactionRequestDto.setPaymentType(type);

                return withdrawalTransactionService.initTransaction(initTransactionRequestDto);
            }

            case TRANSFER -> {
                initTransactionRequestDto.setPaymentType(type);

                return transferTransactionService.initTransaction(initTransactionRequestDto);
            }

            default -> {
                return Mono.error(new IllegalStateException(String.format("Unexpected payment type: [%s]", type)));
            }
        }
    }

    public Mono<InitTransactionResponseDto> initTransactionFallback(
            PaymentType type,
            InitTransactionRequestDto initTransactionRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
            String action = "init-transaction-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while transaction was initialized. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    @RateLimiter(
            name = "confirm-transaction-rate-limiter",
            fallbackMethod = "confirmTransactionFallback"
    )
    @CircuitBreaker(
            name = "confirm-transaction-circuitbreaker",
            fallbackMethod = "confirmTransactionFallback"
    )
    public Mono<UUID> confirmTransaction(
            PaymentType type,
            ConfirmTransactionRequestDto confirmTransactionRequestDto
    ) {
        switch(type) {
            case DEPOSIT -> {
                confirmTransactionRequestDto.setPaymentType(type);

                return depositTransactionService.confirmTransaction(confirmTransactionRequestDto);
            }

            case WITHDRAWAL -> {
                confirmTransactionRequestDto.setPaymentType(type);

                return withdrawalTransactionService.confirmTransaction(confirmTransactionRequestDto);
            }

            case TRANSFER -> {
                confirmTransactionRequestDto.setPaymentType(type);

                return transferTransactionService.confirmTransaction(confirmTransactionRequestDto);
            }

            default -> {
                return Mono.error(new IllegalStateException(String.format("Unexpected payment type: [%s]", type)));
            }
        }
    }

    public Mono<UUID> confirmTransactionFallback(
            PaymentType type,
            ConfirmTransactionRequestDto confirmTransactionRequestDto,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
            String action = "confirm-transaction-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while transaction was confirmed. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    @RateLimiter(
            name = "get-transaction-status-rate-limiter",
            fallbackMethod = "getTransactionStatusFallback"
    )
    @CircuitBreaker(
            name = "get-transaction-status-circuitbreaker",
            fallbackMethod = "getTransactionStatusFallback"
    )
    public Mono<TransactionStatus> getTransactionStatus(
            UUID id
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> transactionClient.getTransactionStatus(id, traceId, authorization)
                                .flatMap(response -> Mono.just(Objects.requireNonNull(response.getBody())))));
    }

    private Mono<TransactionStatus> getTransactionStatusFallback(
            UUID id,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
            String action = "get-transaction-status-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while transaction status was gotten. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }

    @RateLimiter(
            name = "get-transactions-rate-limiter",
            fallbackMethod = "getTransactionsFallback"
    )
    @CircuitBreaker(
            name = "get-transactions-circuitbreaker",
            fallbackMethod = "getTransactionsFallback"
    )
    public Mono<Flux<TransactionInfoResponseDto>> getTransactions(
            UUID userId,
            UUID walletId,
            PaymentType type,
            TransactionStatus status,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Integer page,
            Integer size
    ) {
        return fetchTraceId()
                .flatMap(traceId -> keycloakClient.getAdminBearerToken()
                        .flatMap(authorization -> transactionClient.getTransactions(traceId, authorization, userId, walletId, type, status, dateFrom, dateTo, page, size)
                                .map(response -> Objects.requireNonNull(response.getBody())
                                        .flatMap(Flux::just))));
    }

    private Mono<Flux<TransactionInfoResponseDto>> getTransactionsFallback(
            UUID userId,
            UUID walletId,
            PaymentType type,
            TransactionStatus status,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Integer page,
            Integer size,
            Exception exception
    ) {
        return Mono.deferContextual(context -> {
            String traceId = context.getOrDefault(X_TRACE_ID_HEADER, TraceFactory.genTraceId());
            String action = "get-transactions-fallback";
            String exceptionClassSimpleName = exception.getClass().getSimpleName();
            String exceptionMessage = exception.getMessage();

            log.error("[{}][{} -> {}]: {} occurred while transactions were gotten. " +
                            "Fallback was called. Exception: {}",
                    traceId, serviceName, action, exceptionClassSimpleName, exceptionMessage);

            return Mono.error(() -> new FallbackOperationException(exceptionMessage));
        });
    }
}