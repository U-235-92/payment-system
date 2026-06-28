package aq.project.util.aspects;

import aq.project.dto.CreateWalletRequestDTO;
import aq.project.dto.WalletInfoResponseDTO;
import aq.project.util.telemetry.ServiceAspectHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class WalletServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.WalletService.createWallet(..)) && args(createWalletRequestDTO)")
    public Mono<String> createWallet(
            ProceedingJoinPoint pjp,
            @Valid CreateWalletRequestDTO createWalletRequestDTO
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "create-wallet";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String personId = createWalletRequestDTO.getPersonId();
        String preMainLogicLogMessage = String.format("Received request to create a wallet by a person with id: %s",
                personId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to create a wallet by a person with id: %s",
                personId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to create a wallet by a person with id: %s",
                personId);;
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

    @Around("execution(* aq.project.services.WalletService.getWalletInfo(..)) && args(walletId)")
    public Mono<WalletInfoResponseDTO> getWalletInfo(
            ProceedingJoinPoint pjp,
            @NotBlank @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") String walletId
            ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-wallet-info";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get wallet info with id: %s",
                walletId);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to get wallet info with id: %s",
                walletId);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to get wallet info with id: %s",
                walletId);;
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
