package aq.project.controllers.advices;

import aq.project.controllers.DevRestController;
import aq.project.dto.ErrorDTO;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Profile("dev")
@RequiredArgsConstructor
@RestControllerAdvice(assignableTypes = DevRestController.class)
public class DevRestControllerExceptionHandler {

    private final OpenTelemetry openTelemetry;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDTO> onUserExistsException(RuntimeException e) {
        logException(e);
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(badRequest.value()).body(getErrorDTO(e, badRequest, e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDTO> onMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logException(e);
        HttpStatus conflict = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(conflict.value()).body(getErrorDTO(e, conflict, e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> onHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logException(e);
        HttpStatus conflict = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(conflict.value()).body(getErrorDTO(e, conflict, e.getMessage()));
    }

    private ErrorDTO getErrorDTO(Exception exception, HttpStatus httpStatus, String message) {
        return new ErrorDTO(exception.getClass(), httpStatus.value(), message);
    }

    private void logException(Exception exception) {
        Tracer tracer = openTelemetry.getTracer("person-service.exception-tracer");
        Span span = tracer.spanBuilder("exception-span").startSpan();
        span.recordException(exception);
        String logMessage = String.format("%s occurred at: %s", exception.getClass().getName(), exception.getMessage());
        log.warn(logMessage);
        span.end();
    }
}
