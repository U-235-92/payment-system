package aq.project.aspects;

import aq.project.dto.CreateUserEvent;
import aq.project.dto.UpdateUserEvent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Aspect
@Component
public class KeycloakClientLoggingAspect {

    @Around(value = "execution(* aq.project.proxies.KeycloakClient.createUser(..))")
    public Mono<?> createUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        CreateUserEvent createUserRequest = (CreateUserEvent) pjp.getArgs()[0];
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("User with email %s was registered successfully.", createUserRequest.getIndividualData().getEmail())))
                .doOnError(this::logError);
    }

    @Around(value = "execution(* aq.project.proxies.KeycloakClient.loginUser(..))")
    public Mono<?> loginUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        String email = (String) pjp.getArgs()[0];
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("User with email %s was logged in successfully.", email)))
                .doOnError(this::logError);
    }

    @Around(value = "execution(* aq.project.proxies.KeycloakClient.updateUser(..))")
    public Mono<?> updateUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        UpdateUserEvent updateUserEvent = (UpdateUserEvent) pjp.getArgs()[0];
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("User with keycloakId %s was updated successfully.", updateUserEvent.getKeycloakUserId())))
                .doOnError(this::logError);
    }

    @Around(value = "execution(* aq.project.proxies.KeycloakClient.deleteUserByKeycloakId(..))")
    public Mono<?> deleteUserByKeycloakIdAspect(ProceedingJoinPoint pjp) throws Throwable {
        String keycloakUserId = (String) pjp.getArgs()[0];
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info(String.format("User with keycloakId %s was deleted successfully.", keycloakUserId)))
                .doOnError(this::logError);
    }

    @Around(value = "execution(* aq.project.proxies.KeycloakClient.refreshToken(..))")
    public Mono<?> refreshTokenAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(obj -> log.info("Token was refreshed successfully."))
                .doOnError(this::logError);
    }

    private void logError(Throwable exception) {
        log.error(exception.getMessage(), exception);
    }
}
