package aq.project.aspects;

import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.telemetry.ServiceAspectHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class WalletServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.WalletService.createWallet(..)) " +
            "&& args(createWalletRequestDto)")
    public Mono<UUID> createWallet(
            ProceedingJoinPoint pjp,
            @NotNull @Valid CreateWalletRequestDto createWalletRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = String.format(
                "Received request for create wallet with user id: [%s]",
                createWalletRequestDto.getUserId());

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle of request for create wallet with user id: [%s]",
                createWalletRequestDto.getUserId());

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred while handle of request for create wallet with user id: [%s]",
                createWalletRequestDto.getUserId());

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    @Around("execution(* aq.project.services.WalletService.getWalletInfo(..)) " +
            "&& args(walletId)")
    public Mono<WalletInfoResponseDto> getWalletInfo(
            ProceedingJoinPoint pjp,
            @NotNull UUID walletId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet-info";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = String.format(
                "Received request for get wallet info with wallet id: [%s]", walletId);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle of request for get wallet info with wallet id: [%s]", walletId);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred while handle of request for get wallet info with wallet id: [%s]", walletId);

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }

    @Around("execution(* aq.project.services.WalletService.getWalletCurrencyCode(..)) " +
            "&& args(walletId)")
    public Mono<String> getWalletCurrencyCode(
            ProceedingJoinPoint pjp,
            @NotNull UUID walletId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet-currency-code";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String preMainLogicLogMessage = String.format(
                "Received request for get wallet currency code with wallet id: [%s]", walletId);

        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle of request for get wallet currency code with wallet id: [%s]", walletId);

        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred while handle of request for get wallet currency code with wallet id: [%s]", walletId);

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null
        );
    }
}