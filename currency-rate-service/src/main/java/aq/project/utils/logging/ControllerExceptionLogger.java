package aq.project.utils.logging;

import aq.project.utils.telemetry.TraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerExceptionLogger {

    private final TraceContext traceContext;

    public void logException(Exception exception, HttpStatus status) {
        String traceId = traceContext.getTraceId();
        String exceptionClassName = exception.getClass().getSimpleName();
        String exceptionCause = (exception.getCause() == null)
                ? ""
                : "Cause: " + exception.getCause().getClass().getName() + ": " + exception.getCause().getMessage();
        String statusCodeMessage = (status == null)
                ? ""
                : "http status: " + status.value() + "; ";
        String exceptionMessage = exception.getMessage() + "; " + exceptionCause;
        log.error("[{}]: {}{} occurred at: {}",
                traceId, statusCodeMessage, exceptionClassName, exceptionMessage);
    }
}
