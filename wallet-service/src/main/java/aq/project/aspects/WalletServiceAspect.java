package aq.project.aspects;

import aq.project.dto.*;
import aq.project.utils.telemetry.ServiceAspectHandler;
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

import java.util.List;
import java.util.UUID;

@Aspect
@Order(1)
@Validated
@Component
@RequiredArgsConstructor
public class WalletServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.WalletService.createWallet(..)) " +
            "&& args(createWalletRequestDto)")
    public UUID createWallet(
            ProceedingJoinPoint pjp,
            @NotNull @Valid CreateWalletRequestDto createWalletRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to create a new wallet for user with id: [%s]",
                createWalletRequestDto.getUserId());

//        Handler logic call
        return serviceAspectHandler.handle(
                UUID.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.WalletService.getWalletInfo(..)) " +
            "&& args(walletId)")
    public WalletInfoResponseDto getWalletInfo(
            ProceedingJoinPoint pjp,
            @NotNull UUID walletId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet-info";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to get wallet info for wallet with id: [%s]",
                walletId);

//        Handler logic call
        return serviceAspectHandler.handle(
                WalletInfoResponseDto.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.WalletService.getWalletCurrencyCode(..)) " +
            "&& args(walletId)")
    public String getWalletCurrencyCode(
            ProceedingJoinPoint pjp,
            @NotNull UUID walletId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet-currency-code";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to get wallet currency for wallet with id: [%s]",
                walletId);

//        Handler logic call
        return serviceAspectHandler.handle(
                String.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.WalletService.depositWallet(..)) " +
            "&& args(depositWalletRequestDto)")
    public UUID depositWallet(
            ProceedingJoinPoint pjp,
            @NotNull @Valid DepositWalletRequestDto depositWalletRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "deposit-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to deposit sufficient on wallet with id: [%s]",
                depositWalletRequestDto.getWalletId());

//        Handler logic call
        return serviceAspectHandler.handle(
                UUID.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.WalletService.withdrawalWallet(..)) " +
            "&& args(withdrawalWalletRequestDto)")
    public UUID withdrawalWallet(
            ProceedingJoinPoint pjp,
            @NotNull @Valid WithdrawalWalletRequestDto withdrawalWalletRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "withdrawal-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to withdrawal sufficient on wallet with id: [%s]",
                withdrawalWalletRequestDto.getWalletId());

//        Handler logic call
        return serviceAspectHandler.handle(
                UUID.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }

    @Around("execution(* aq.project.services.WalletService.transferWallet(..)) " +
            "&& args(transferWalletRequestDto)")
    public List<UUID> transferWallet(
            ProceedingJoinPoint pjp,
            @NotNull @Valid TransferWalletRequestDto transferWalletRequestDto
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "transfer-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";

        String logOnBeginning = String.format("Received request to transfer sufficient " +
                        "from wallet with id: [%s] to wallet with id: [%s]",
                transferWalletRequestDto.getSourceWalletId(), transferWalletRequestDto.getTargetWalletId());

//        Handler logic call
        return serviceAspectHandler.handle(
                List.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                logOnBeginning,
                null
        );
    }
}
