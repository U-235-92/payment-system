package aq.project.utils.aspects;

import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import aq.project.utils.telemetry.ServiceAspectHandler;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static aq.project.utils.constants.CustomConstants.ISO_DATE_FORMAT;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class CurrencyRateServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.CurrencyRateService.getCurrencies(..)) && args()")
    public Mono<Flux<CurrencyResponse>> getCurrencies(
            ProceedingJoinPoint pjp
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-currencies";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to get currencies list";
        String postSuccessMainLogicCallLogMessage = "Success handle request to get currencies list";
        String postFailureMainLogicCallLogMessage = "Error occurred during handle request to get currencies list";
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

    @Around("execution(* aq.project.services.CurrencyRateService.getCurrencyInfo(..)) && args(code)")
    public Mono<CurrencyResponse> getCurrencyInfo(
            ProceedingJoinPoint pjp,
            @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]{3}$") String code
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-currency-info";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get currency info for currency with code: %s",
                code);
        String postSuccessMainLogicCallLogMessage = String.format("Success finish handle request to get currency info for currency with code: %s",
                code);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to get currency info for currency with code: %s",
                code);
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

    @Around("execution(* aq.project.services.CurrencyRateService.getRate(..)) && args(from, to, provider, date)")
    public Mono<RateResponse> getRate(
            ProceedingJoinPoint pjp,
            @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]{3}$") String from,
            @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]{3}$") String to,
            @Nullable String provider,
            @Nullable OffsetDateTime date
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-rate";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String optProvider = Optional.ofNullable(provider).orElse("[not assigned]");
        String optRateDate = Optional.ofNullable(date)
                .map(d -> DateTimeFormatter.ofPattern(ISO_DATE_FORMAT).format(date))
                .orElse("[not assigned]");
        String preMainLogicLogMessage = String.format("Received request to get rate: [%s -> %s], provider: %s, rate date: %s",
                from, to, optProvider, optRateDate);
        String postSuccessMainLogicCallLogMessage = String.format("Success handle request to get rate: [%s -> %s], provider: %s, rate date: %s",
                from, to, optProvider, optRateDate);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to get rate: [%s -> %s], provider: %s, rate date: %s",
                from, to, optProvider, optRateDate);
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

    @Around("execution(* aq.project.services.CurrencyRateService.getRateProviderInfo(..)) && args(code)")
    public Mono<RateProviderResponse> getRateProviderInfo(
            ProceedingJoinPoint pjp,
            @NotBlank @Size(min = 3, max = 10) @Pattern(regexp = "^[A-Z]{3,10}$") String code
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-rate-provider-info";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = String.format("Received request to get rate provider info with code: %s",
                code);
        String postSuccessMainLogicCallLogMessage = String.format("Success finish handle request to get rate provider info with code: %s",
                code);
        String postFailureMainLogicCallLogMessage = String.format("Error occurred during handle request to get rate provider info with code: %s",
                code);
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

    @Around("execution(* aq.project.services.CurrencyRateService.getRateProviders(..)) && args()")
    public Mono<Flux<RateProviderResponse>> getRateProviders(
            ProceedingJoinPoint pjp
    ) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-rate-providers";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to get rate providers list";
        String postSuccessMainLogicCallLogMessage = "Success handle request to get rate providers list";
        String postFailureMainLogicCallLogMessage = "Error occurred during handle request to get rate providers list";
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
