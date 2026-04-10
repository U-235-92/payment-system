package aq.project.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@Profile("dev")
public class DevRestControllerLogAspect {

    @Around("execution(* aq.project.controllers.DevRestController.*(..))")
    public Object aroundAnyMethod(ProceedingJoinPoint pjp) throws Throwable {
        log.info("Before @Around aspect method " + pjp.getSignature().getName());
        log.info("Parameters: " + Arrays.toString(pjp.getArgs()));
        Object result = pjp.proceed();
        log.info("After @Around aspect method " + pjp.getSignature().getName());
        return result;
    }

// Дубликат логики работы обработки исключения обработчика DevRestControllerExceptionHandler! (смотри логи консоли)
// Обработку исключений следует вынести в обработчика! Аспекты не должны дублировать эту логику!
    @AfterThrowing(value = "execution(* aq.project.controllers.DevRestController.*(..))", throwing = "e")
    public void aroundAnyMethod(JoinPoint joinPoint, Exception e) throws Throwable {
        log.warn("After @AfterThrowing aspect method: " + joinPoint.getSignature().getName() + ", exception: " + e);
    }
}
