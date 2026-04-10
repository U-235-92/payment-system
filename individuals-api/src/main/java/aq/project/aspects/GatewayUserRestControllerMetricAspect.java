package aq.project.aspects;

import aq.project.exceptions.IncorrectUserCredentialsException;
import aq.project.exceptions.UserExistsException;
import aq.project.util.metrics.ApplicationMetricsRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Component
@RequiredArgsConstructor
public class GatewayUserRestControllerMetricAspect {

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

//    Changed on @Timed annotation under all the methods of GatewayUserRestController
//    @Around(value = "execution(* aq.project.controllers.GatewayUserRestController.*(..))")
//    public Object requestLatencyAspect(ProceedingJoinPoint pjp) {
//        return Mono.just(System.nanoTime()).flatMap(start -> evaluateRequestLatency(start, pjp));
//    }
//
//    private Mono<?> evaluateRequestLatency(long start, ProceedingJoinPoint pjp) {
//        try {
//            return ((Mono<?>) pjp.proceed()).doOnNext(obj -> evaluateRequestLatency(start));
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void evaluateRequestLatency(long start) {
//        long end = System.nanoTime();
//        applicationMetricsRegistry.evaluateRequestLatency(start, end);
//    }

    @Around(value = "execution(* aq.project.controllers.GatewayUserRestController.createUser(..))")
    public Mono<?> createUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(this::incrementSuccessCreateUserAndLoginUserCounters)
                .doOnError(this::incrementFailCreateUserCount);
    }

    private void incrementSuccessCreateUserAndLoginUserCounters(Object obj) {
        incrementSuccessCreateUserCount(obj);
        incrementSuccessLoginUserCount(obj);
    }

    private void incrementSuccessCreateUserCount(Object obj) {
        if(((ResponseEntity<?>) obj).getStatusCode().value() == HttpStatus.SC_CREATED) {
            applicationMetricsRegistry.incrementSuccessCreateUserCounter();
        }
    }

    private void incrementFailCreateUserCount(Throwable exception) {
        if(exception instanceof UserExistsException) {
            applicationMetricsRegistry.incrementFailCreateUserCounter();
        }
    }

    @Around(value = "execution(* aq.project.controllers.GatewayUserRestController.loginUser(..))")
    public Object countLoginUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(this::incrementSuccessLoginUserCount)
                .doOnError(this::incrementFailLoginCount);
    }

    private void incrementSuccessLoginUserCount(Object obj) {
        if(((ResponseEntity<?>) obj).getStatusCode().value() == HttpStatus.SC_OK) {
            applicationMetricsRegistry.incrementSuccessLoginUserCounter();
        }
    }

    private void incrementFailLoginCount(Throwable exception) {
        if(exception instanceof IncorrectUserCredentialsException) {
            applicationMetricsRegistry.incrementFailLoginUserCounter();
        }
    }

    @Around(value = "execution(* aq.project.controllers.GatewayUserRestController.updateUser(..))")
    public Object countUpdateUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(this::incrementSuccessUpdateUserCount)
                .doOnError(this::incrementFailUpdateCount);
    }

    private void incrementSuccessUpdateUserCount(Object obj) {
        if(((ResponseEntity<?>) obj).getStatusCode().value() == HttpStatus.SC_OK) {
            applicationMetricsRegistry.incrementSuccessUpdateUserCounter();
        }
    }

    private void incrementFailUpdateCount(Throwable throwable) {
        applicationMetricsRegistry.incrementFailUpdateUserCounter();
    }

    @Around(value = "execution(* aq.project.controllers.GatewayUserRestController.deleteUserByKeycloakId(..))")
    public Object countDeleteUserByKeycloakIdAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(this::incrementSuccessDeleteUserByKeycloakIdCount)
                .doOnError(this::incrementFailDeleteUserByKeycloakIdCount);
    }

    private void incrementSuccessDeleteUserByKeycloakIdCount(Object obj) {
        if(((ResponseEntity<?>) obj).getStatusCode().value() == HttpStatus.SC_OK) {
            applicationMetricsRegistry.incrementSuccessDeleteUserByKeycloakIdCounter();
        }
    }

    private void incrementFailDeleteUserByKeycloakIdCount(Throwable throwable) {
        applicationMetricsRegistry.incrementFailDeleteUserByKeycloakIdCounter();
    }

    @Around(value = "execution(* aq.project.controllers.GatewayUserRestController.getUserInfo(..))")
    public Object countGetUserInfoAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(this::incrementSuccessGetUserInfoCount)
                .doOnError(this::incrementFailGetUserInfoCount);
    }

    private void incrementSuccessGetUserInfoCount(Object obj) {
        if(obj != null) {
            if(((ResponseEntity<?>) obj).getStatusCode().value() == HttpStatus.SC_OK) {
                applicationMetricsRegistry.incrementSuccessGetUserInfoCounter();
            }
        } else {
            applicationMetricsRegistry.incrementFailGetUserInfoCounter();
        }
    }

    private void incrementFailGetUserInfoCount(Throwable throwable) {
        applicationMetricsRegistry.incrementFailGetUserInfoCounter();
    }

    @Around(value = "execution(* aq.project.controllers.GatewayUserRestController.refreshToken(..))")
    public Object countRefreshTokenAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(this::incrementSuccessRefreshTokenCount)
                .doOnError(this::incrementFailRefreshTokenCount);
    }

    private void incrementSuccessRefreshTokenCount(Object obj) {
        if(((ResponseEntity<?>) obj).getStatusCode().value() == HttpStatus.SC_OK) {
            applicationMetricsRegistry.incrementSuccessRefreshTokenCounter();
        }
    }

    private void incrementFailRefreshTokenCount(Throwable throwable) {
        applicationMetricsRegistry.incrementFailRefreshTokenCounter();
    }
}
