package aq.project.utils.aspects.wallet_service;

import aq.project.dto.CreateWalletRequestDto;
import aq.project.dto.IndividualsApiServiceDepositTransactionRequestDto;
import aq.project.dto.WalletInfoResponseDto;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolationException;
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

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Supplier;

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class WalletServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.wallets.WalletService.createWallet(..)) && args(dto)")
    public Mono<String> createWallet(
            ProceedingJoinPoint pjp,
            @NotNull @Valid CreateWalletRequestDto dto
    ) throws Throwable {
//        Prepare handler metadata
        UUID personId = dto.getPersonId();

        String actionName = "create-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format(
                "Received request to create a wallet by a person with id: %s",
                personId);
        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle request to create a wallet by a person with id: %s",
                personId);
        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle request to create a wallet by a person with id: %s",
                personId);

//        Handler logic call
        return serviceAspectHandler.handle(
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                validateCreateWalletRequestDto(dto)
        );
    }

    private Supplier<Mono<Void>> validateCreateWalletRequestDto(
            CreateWalletRequestDto dto
    ) {
        return () -> {
            BigDecimal balance = dto.getBalance();

            if(balance.compareTo(new BigDecimal(0)) <= 0) {
                String logMessageOnError = String.format(
                        "Received create wallet request with invalid balance value: [%s]",
                        balance);
                return Mono.error(new ConstraintViolationException(logMessageOnError, null));
            }
            return Mono.empty();
        };
    }

    @Around("execution(* aq.project.services.wallets.WalletService.getWalletInfo(..)) && args(walletId)")
    public Mono<WalletInfoResponseDto> getWalletInfo(
            ProceedingJoinPoint pjp,
            @NotNull UUID walletId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet-info";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format(
                "Received request to get wallet info with id: %s",
                walletId);
        String postSuccessMainLogicCallLogMessage = String.format(
                "Success handle request to get wallet info with id: %s",
                walletId);
        String postFailureMainLogicCallLogMessage = String.format(
                "Error occurred during handle request to get wallet info with id: %s",
                walletId);

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

    @Around("execution(* aq.project.services.wallets.WalletService.getWalletCurrencyCode(..)) && args(walletId)")
    public Mono<String> getWalletCurrencyCode(
            ProceedingJoinPoint pjp,
            @NotNull UUID walletId
    ) throws Throwable {
        return (Mono<String>) pjp.proceed();
    }
}
