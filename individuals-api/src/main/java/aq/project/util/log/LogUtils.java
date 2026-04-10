package aq.project.util.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;

public abstract class LogUtils {

    public static void logAspectError(ProceedingJoinPoint pjp, Throwable exc, Logger logger, String traceId, String spanId) {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringType().getName();
        logger.error(String.format("[%s-%s] %s occurred during call method [%s.%s]: %s",
                traceId, spanId, exc.getClass().getSimpleName(), className, methodName, exc.getMessage()));
    }
}
