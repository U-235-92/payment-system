package aq.project.utils.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerExceptionLogger {

    public void logException(Exception exception, HttpStatus status) {
        String exceptionClassName = exception.getClass().getSimpleName();
        String exceptionCause = exception.getCause() == null ? "" : "Cause: " + exception.getCause().getClass().getName() + ": " + exception.getCause().getMessage();
        String exceptionMessage = exception.getMessage() + "; " + exceptionCause;

        log.error("{} occurred at: {}", exceptionClassName, exceptionMessage);
    }

    public void logException(String traceId, String service, String action, Exception exception) {
        String exceptionClassSimpleName = exception.getClass().getSimpleName();
        String exceptionClassFullName = exception.getCause().getClass().getName();
        String exceptionMessage;
        if(exception.getCause() != null) {
            String onNotNullCauseMessage = String.format(
                    "Cause: %s: %s", exceptionClassFullName, exception.getCause().getMessage());
            exceptionMessage = exception.getMessage() + "; " + onNotNullCauseMessage;
        } else {
            exceptionMessage = exception.getMessage() + ";";
        }
        log.error("[{}][{} -> {}]: {} occurred at: {}",
                traceId, service, action, exceptionClassSimpleName, exceptionMessage);
    }
}
