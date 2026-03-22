package aq.project.controllers.advices;

import aq.project.controllers.DevRestController;
import aq.project.dto.ErrorDTO;
import aq.project.exceptions.IllegalUndoEventPayloadPropertyException;
import aq.project.exceptions.NotFoundRevisionException;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Profile("dev")
@RequiredArgsConstructor
@RestControllerAdvice(assignableTypes = DevRestController.class)
public class DevRestControllerExceptionHandler {

    private final OpenTelemetry openTelemetry;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDTO> onRuntimeException(RuntimeException e) {
        logException(e);
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(badRequest.value()).body(getErrorDTO(badRequest, e.getMessage()));
    }

    @ExceptionHandler(NotFoundRevisionException.class)
    public ResponseEntity<ErrorDTO> onLackRevisionException(NotFoundRevisionException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalUndoEventPayloadPropertyException.class)
    public ResponseEntity<ErrorDTO> onIllegalUndoEventPayloadPropertyException(IllegalUndoEventPayloadPropertyException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    private ErrorDTO getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDTO(httpStatus.value(), message);
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
