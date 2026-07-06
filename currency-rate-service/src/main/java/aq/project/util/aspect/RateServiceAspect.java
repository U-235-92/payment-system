package aq.project.util.aspect;

import aq.project.dto.CurrencyResponse;
import aq.project.dto.RateProviderResponse;
import aq.project.dto.RateResponse;
import aq.project.util.telemetry.ServiceAspectHandler;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static aq.project.util.constants.CustomConstants.ISO_DATE_FORMAT;

@Aspect
@Component
@Validated
@RequiredArgsConstructor
public class RateServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.service.RateService.getRate(..)) && args(from, to, provider, date)")
    public RateResponse getRate(
            ProceedingJoinPoint pjp,
            @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]{3}$") String from,
            @NotBlank @Size(min = 3, max = 3) @Pattern(regexp = "^[A-Z]{3}$") String to,
            @Nullable String provider,
            @Nullable LocalDate date
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
                RateResponse.class,
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

    @Around("execution(* aq.project.service.RateService.getCurrencies(..)) && args()")
    public List<?> getCurrencies(ProceedingJoinPoint pjp) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-currencies";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to get currencies list";
        String postSuccessMainLogicCallLogMessage = "Success handle request to get currencies list";
        String postFailureMainLogicCallLogMessage = "Error occurred during handle request to get currencies list";
//        Handler logic call
        return serviceAspectHandler.handle(
                List.class,
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

    @Around("execution(* aq.project.service.RateService.getRateProviders(..)) && args()")
    public List<?> getRateProviders(ProceedingJoinPoint pjp) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-rate-providers";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to get rate providers list";
        String postSuccessMainLogicCallLogMessage = "Success handle request to get rate providers list";
        String postFailureMainLogicCallLogMessage = "Error occurred during handle request to get rate providers list";
//        Handler logic call
        return serviceAspectHandler.handle(
                List.class,
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

    @Around("execution(* aq.project.service.RateService.updateCurrencyRates(..)) && args()")
    public void updateCurrencyRates(ProceedingJoinPoint pjp) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-rate-providers";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Schedule task to update currency rates";
        String postSuccessMainLogicCallLogMessage = "Success finish task to update currency rates";
        String postFailureMainLogicCallLogMessage = "Error occurred during processing task to update currency rates";
//        Handler logic call
        serviceAspectHandler.handle(
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

    @Around("execution(* aq.project.service.RateService.getCurrencyInfo(..)) && args(code)")
    public CurrencyResponse getCurrencyInfo(
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
                CurrencyResponse.class,
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

    @Around("execution(* aq.project.service.RateService.getRateProviderInfo(..)) && args(code)")
    public RateProviderResponse getRateProviderInfo(
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
                RateProviderResponse.class,
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
