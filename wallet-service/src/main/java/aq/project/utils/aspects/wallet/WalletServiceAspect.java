package aq.project.utils.aspects.wallet;

import aq.project.entities.wallet.CreditCard;
import aq.project.entities.wallet.Wallet;
import aq.project.entities.wallet.WalletDetails;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Set;
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

    private final Validator validator;

    @Around("execution(* aq.project.services.wallet.WalletService.createWallet(..)) && args(wallet)")
    public String createWallet(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Wallet wallet
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";
        UUID personId = wallet.getPersonId();
        String preMainLogicLogMessage = String.format("Received request to create wallet by person with id: %s",
                personId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle of request to create wallet by person with id: %s",
                personId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during creating wallet by person with id: %s",
                personId);

//        Handler logic call
        return serviceAspectHandler.handle(
                String.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                checkConstraints(wallet),
                null,
                null
        );
    }

    private Supplier<Void> checkConstraints(Wallet wallet) {
        Set<ConstraintViolation<CreditCard>> creditCardViolations = validator.validate(wallet.getCreditCard());
        Set<ConstraintViolation<WalletDetails>> walletDetailsViolations = validator.validate(wallet.getWalletDetails());
        if(!creditCardViolations.isEmpty())
            throw new ConstraintViolationException(creditCardViolations);

        if(!walletDetailsViolations.isEmpty())
            throw new ConstraintViolationException(walletDetailsViolations);
        return null;
    }

    @Around("execution(* aq.project.services.wallet.WalletService.getWallet(..)) && args(id)")
    public Wallet getWallet(
            ProceedingJoinPoint pjp,
            @NotNull UUID id
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get wallet info with id: %s",
                id);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle of request to get wallet info with id: %s",
                id);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during getting wallet info with id: %s",
                id);

//        Handler logic call
        return serviceAspectHandler.handle(
                Wallet.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }

    @Around("execution(* aq.project.services.wallet.WalletService.getWalletWithLock(..)) && args(id)")
    public Wallet getWalletWithLock(
            ProceedingJoinPoint pjp,
            @NotNull UUID id
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet-with-lock";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get wallet info with id: %s",
                id);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle of request to get wallet info with id: %s",
                id);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during getting wallet info with id: %s",
                id);

//        Handler logic call
        return serviceAspectHandler.handle(
                Wallet.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }

    @Around("execution(* aq.project.services.wallet.WalletService.getWalletCurrencyCode(..)) && args(id)")
    public String getWalletCurrencyCode(
            ProceedingJoinPoint pjp,
            @NotNull UUID id
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet-currency";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get wallet currency with id: %s",
                id);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle of request to get wallet currency with id: %s",
                id);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during getting wallet currency with id: %s",
                id);

//        Handler logic call
        return serviceAspectHandler.handle(
                String.class,
                pjp,
                tracerName,
                serviceName,
                actionName,
                preMainLogicLogMessage,
                postSuccessMainLogicCallLogMessage,
                postFailureMainLogicCallLogMessage,
                null,
                null,
                null
        );
    }
}
