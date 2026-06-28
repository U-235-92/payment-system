package aq.project.util;

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
}
