package aq.project.controllers.advices;

import aq.project.controllers.PersonRestController;
import aq.project.dto.ErrorDTO;
import aq.project.exceptions.CountryNotExistsException;
import aq.project.exceptions.UserExistsException;
import aq.project.exceptions.UserNotExistsException;
import aq.project.util.Observability;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(assignableTypes =  PersonRestController.class)
public class PersonRestControllerExceptionHandler {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorDTO> onUserExistsException(UserExistsException e) {
        HttpStatus conflict = HttpStatus.CONFLICT;
        logException(e, conflict);
        return ResponseEntity.status(conflict.value()).body(getErrorDTO(e, conflict, e.getMessage()));
    }

    @ExceptionHandler(UserNotExistsException.class)
    public ResponseEntity<ErrorDTO> onUserNotExistsException(UserNotExistsException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        logException(e, badRequest);
        return ResponseEntity.status(badRequest.value()).body(getErrorDTO(e, badRequest, e.getMessage()));
    }

    @ExceptionHandler(CountryNotExistsException.class)
    public ResponseEntity<ErrorDTO> onCountryNotExistsException(CountryNotExistsException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        logException(e, badRequest);
        return ResponseEntity.status(badRequest.value()).body(getErrorDTO(e, badRequest, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        logException(e, badRequest);
        return ResponseEntity.status(badRequest.value()).body(getErrorDTO(e, badRequest, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        logException(e, badRequest);
        return ResponseEntity.status(badRequest.value()).body(getErrorDTO(e, badRequest, e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDTO> onMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        logException(e, badRequest);
        return ResponseEntity.status(badRequest.value()).body(getErrorDTO(e, badRequest, e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> onHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        logException(e, badRequest);
        return ResponseEntity.status(badRequest.value()).body(getErrorDTO(e, badRequest, e.getMessage()));
    }

    private ErrorDTO getErrorDTO(Exception exception, HttpStatus httpStatus, String message) {
        return new ErrorDTO(exception.getClass(), httpStatus.value(), message);
    }

    private void logException(Exception exception, HttpStatus status) {
        Tracer tracer = openTelemetry.getTracer(applicationName + ".exception-tracer");
        Span span = tracer.spanBuilder("exception-span").startSpan();
        span.setStatus(StatusCode.ERROR, String.format("%d: %s", status.value(), status.getReasonPhrase()));
        span.recordException(exception);
        String traceId = Observability.getTraceId(span);
        String spanId = Observability.getSpanId(span);
        String exceptionClassName = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();
        String logMessage = String.format("[%s-%s] %s occurred at: %s", traceId, spanId, exceptionClassName, exceptionMessage);
        log.warn(logMessage);
        span.end();
    }
}
