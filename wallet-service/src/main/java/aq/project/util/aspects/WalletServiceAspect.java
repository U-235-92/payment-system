package aq.project.util.aspects;

import aq.project.entities.CreditCard;
import aq.project.entities.Wallet;
import aq.project.entities.WalletDetails;
import aq.project.util.telemetry.ServiceAspectHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Set;
import java.util.function.Supplier;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class WalletServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    private final Validator validator;

    @Around("execution(* aq.project.services.WalletService.createWallet(..)) && args(wallet)")
    public String aspectCreateWallet(
            ProceedingJoinPoint pjp,
            @NotNull @Valid Wallet wallet
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String personId = wallet.getPersonId();
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

    @Around("execution(* aq.project.services.WalletService.getWalletInfo(..)) && args(walletId)")
    public Wallet aspectGetWalletInfo(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String walletId
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet-info";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get wallet info with id: %s",
                walletId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle of request to get wallet info with id: %s",
                walletId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during getting wallet info with id: %s",
                walletId);
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
}
