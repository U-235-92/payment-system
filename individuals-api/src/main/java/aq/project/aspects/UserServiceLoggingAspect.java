package aq.project.aspects;

import aq.project.dto.UserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Aspect
@Component
public class LogUserServiceAspect {

    @Around(value = "execution(* aq.project.services.UserService.getUserInfo(..))")
    public Mono<?> userInfoAspect(ProceedingJoinPoint pjp) throws Throwable {
        return ((Mono<UserInfoResponse>) pjp.proceed())
                .doOnNext(response -> doLogOnUserInfoResponse(response))
                .doOnError(exception -> log.error("Attempt of unsuccessful getting user-info. " +
                        exception.getMessage(), exception));
    }

    private void doLogOnUserInfoResponse(UserInfoResponse response) {
        if(response != null) {
            log.info(String.format("User with email %s has requested user-info.", response.getEmail()));
        }
    }
}
