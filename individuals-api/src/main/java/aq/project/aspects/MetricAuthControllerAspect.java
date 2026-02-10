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
public class MetricAuthControllerAspect {

    private final ApplicationMetricsRegistry applicationMetricsRegistry;

    @Around(value = "execution(* aq.project.controllers.AuthController.*(..))")
    public Object requestLatencyAspect(ProceedingJoinPoint pjp) {
        return Mono.just(System.nanoTime()).flatMap(start -> evaluateRequestLatency(start, pjp));
    }

    private Mono<?> evaluateRequestLatency(long start, ProceedingJoinPoint pjp) {
        try {
            return ((Mono<?>) pjp.proceed()).doOnNext(obj -> evaluateRequestLatency(start));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void evaluateRequestLatency(long start) {
        long end = System.nanoTime();
        applicationMetricsRegistry.evaluateRequestLatency(start, end);
    }

    @Around(value = "execution(* aq.project.controllers.AuthController.createUser(..))")
    public Mono<?> createUserAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(this::incrementSuccessRegistrationAndLoginCounters)
                .doOnError(this::incrementFailRegistrationCount);
    }

    private void incrementSuccessRegistrationAndLoginCounters(Object obj) {
        incrementSuccessRegistrationCount(obj);
        incrementSuccessLoginCount(obj);
    }

    private void incrementSuccessRegistrationCount(Object obj) {
        if(((ResponseEntity<?>) obj).getStatusCode().value() == HttpStatus.SC_CREATED) {
            applicationMetricsRegistry.incrementSuccessRegistrationCounter();
        }
    }

    private void incrementFailRegistrationCount(Throwable exception) {
        if(exception instanceof UserExistsException) {
            applicationMetricsRegistry.incrementFailRegistrationCounter();
        }
    }

    @Around(value = "execution(* aq.project.controllers.AuthController.requestToken(..))")
    public Object countLoginAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<?>) pjp.proceed())
                .doOnSuccess(this::incrementSuccessLoginCount)
                .doOnError(this::incrementFailLoginCount);
    }

    private void incrementSuccessLoginCount(Object obj) {
        if(((ResponseEntity<?>) obj).getStatusCode().value() == HttpStatus.SC_OK) {
            applicationMetricsRegistry.incrementSuccessLoginCounter();
        }
    }

    private void incrementFailLoginCount(Throwable exception) {
        if(exception instanceof IncorrectUserCredentialsException) {
            applicationMetricsRegistry.incrementFailLoginCounter();
        }
    }
}
