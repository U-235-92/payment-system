package aq.project.aspects;

import aq.project.aspects.handlers.AbstractAspectHandler;
import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.util.metrics.ApplicationMetricsRegistry;
import io.micrometer.core.annotation.Timed;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class WalletServiceAspect {

    @Value("${spring.application.name}")
    private String tracerName;

    private final Validator validator;

    private final OpenTelemetry openTelemetry;

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    @Timed(value = "individuals_api.create_wallet_time")
    @Around("execution(* aq.project.services.WalletService.createWallet(..)) && args(createWalletRequestDTO)")
    public Mono<String> createWallet(ProceedingJoinPoint pjp, CreateWalletRequestDTO createWalletRequestDTO) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Create wallet request dto is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                return "Received create wallet request";
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                return "Wallet was created successfully";
            }
        };
        String spanName = "create_wallet";
        return handler.handleAspect(
                pjp,
                createWalletRequestDTO,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessCreateWalletCounter,
                ApplicationMetricsRegistry::incrementFailCreateWalletCounter
        );
    }

    @Timed(value = "individuals_api.get_wallet_info_time")
    @Around("execution(* aq.project.services.WalletService.getWalletInfo(..)) && args(walletId)")
    public Mono<WalletInfoResponseDTO> getWalletInfo(ProceedingJoinPoint pjp, String walletId) throws Throwable {
        AbstractAspectHandler handler = new AbstractAspectHandler(validator, openTelemetry, applicationMetricsRegistry) {
            @Override
            protected String getNullArgumentExceptionMessage() {
                return "Wallet id is null";
            }

            @Override
            protected String getPreMainLogicCallMethodMessage() {
                return String.format("Received get wallet info request for wallet id [%s]",
                        walletId);
            }

            @Override
            protected String getAfterSuccessMainLogicCallMessage() {
                return String.format("The handle of get wallet info request with wallet id [%s] completed successfully",
                        walletId);
            }
        };
        String spanName = "get_wallet_info";
        return handler.handleAspect(
                pjp,
                walletId,
                tracerName,
                spanName,
                ApplicationMetricsRegistry::incrementSuccessGetWalletInfoRequestCounter,
                ApplicationMetricsRegistry::incrementFailGetWalletInfoRequestCounter
        );
    }
}
