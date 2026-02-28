package aq.project.aspects;

import aq.project.dto.UserRegistrationRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Aspect
@Component
public class LogKeycloakClientAspect {

    @Around(value = "execution(* aq.project.proxies.KeycloakClient.createUser(..))")
    public Mono<?> userCreationAspect(ProceedingJoinPoint pjp) throws Throwable {
        UserRegistrationRequest request = (UserRegistrationRequest) pjp.getArgs()[0];
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("User with email %s was registered successfully.", request.getEmail())))
                .doOnError(this::logError);
    }

    @Around(value = "execution(* aq.project.proxies.KeycloakClient.login(..))")
    public Mono<?> userLoginAspect(ProceedingJoinPoint pjp) throws Throwable {
        String email = (String) pjp.getArgs()[0];
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("User with email %s was logged in successfully.", email)))
                .doOnError(this::logError);
    }

    @Around(value = "execution(* aq.project.proxies.KeycloakClient.refreshToken(..))")
    public Mono<?> userRefreshTokenAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info("Token was refreshed successfully."))
                .doOnError(this::logError);
    }

    private void logError(Throwable exception) {
        log.error(exception.getMessage(), exception);
    }
}
