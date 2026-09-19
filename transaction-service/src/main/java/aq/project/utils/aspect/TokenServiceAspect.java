package aq.project.utils.aspect;

import aq.project.utils.telemetry.ServiceAspectHandler;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Aspect
@Order(1)
@Component
@Validated
@RequiredArgsConstructor
public class TokenServiceAspect {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ServiceAspectHandler serviceAspectHandler;

    @Around("execution(* aq.project.services.TokenService.getAdminJwt())")
    public String getAdminJwt(ProceedingJoinPoint pjp) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-admin-jwt";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to get admin JWT";
        String postSuccessMainLogicCallLogMessage = "Success request to get admin JWT";
        String postFailureMainLogicCallLogMessage = "Error occurred during getting admin JWT";

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
                null,
                false
        );
    }


    @Around("execution(* aq.project.services.TokenService.getAdminJwtAsAuthorizationHeaderValue())")
    public String getAdminJwtAsAuthorizationHeaderValue(ProceedingJoinPoint pjp) throws Throwable {
//        Prepare handler metadata
        String actionName = "get-admin-jwt-as-authorization-header-value";
        String tracerName = serviceName + "." + actionName + "-tracer";
        String preMainLogicLogMessage = "Received request to get admin JWT";
        String postSuccessMainLogicCallLogMessage = "Success request to get admin JWT";
        String postFailureMainLogicCallLogMessage = "Error occurred during getting admin JWT";

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
                null,
                false
        );
    }
}
