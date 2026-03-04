package aq.project.aspects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PersonControllerLoggingAspect {

    @Pointcut("within(aq.project.controllers.PersonRestController.*)")
    public void personRestControllerPointcut() {
    }

    @Before("execution(* aq.project.controllers.PersonRestController.*)")
    public void onControllerMethodsCall(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());
        log.info(String.format("Call of method %s with args %s", methodName, args));
    }

    @AfterThrowing(pointcut = "personRestControllerPointcut()", throwing = "exc")
    public void onExceptionCallMethodLog(JoinPoint joinPoint, Exception exc) {
        String methodName = joinPoint.getSignature().getName();
        log.warn(String.format("Exception occurred while executing %s", methodName), exc);
    }

    @AfterReturning("execution(* aq.project.controllers.PersonRestController.*)")
    public void onSuccessfulCallMethodLog(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info(String.format("Method %s was completed successfully", methodName));
    }
}
