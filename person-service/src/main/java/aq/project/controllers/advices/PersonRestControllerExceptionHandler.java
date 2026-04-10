package aq.project.controllers.advices;

import aq.project.dto.ErrorDTO;
import aq.project.exceptions.*;
import aq.project.util.observability.ObserverUtils;
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
@RestControllerAdvice
@RequiredArgsConstructor
public class PersonRestControllerExceptionHandler {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorDTO> onUserExistsException(UserExistsException e) {
        HttpStatus status = HttpStatus.CONFLICT;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(UserNotExistsException.class)
    public ResponseEntity<ErrorDTO> onUserNotExistsException(UserNotExistsException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(CountryNotExistsException.class)
    public ResponseEntity<ErrorDTO> onCountryNotExistsException(CountryNotExistsException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDTO> onMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> onHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(NotFoundRevisionException.class)
    public ResponseEntity<ErrorDTO> onNotFoundRevisionException(NotFoundRevisionException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalUndoOperationPayloadPropertyException.class)
    public ResponseEntity<ErrorDTO> onIllegalUndoOperationPayloadPropertyException(IllegalUndoOperationPayloadPropertyException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDTO> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(NotExpectedUndoOperationCallException.class)
    public ResponseEntity<ErrorDTO> onNotExpectedUndoOperationCallException(NotExpectedUndoOperationCallException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(NotFoundUndoOperationCallException.class)
    public ResponseEntity<ErrorDTO> onNotFoundUndoOperationCallException(NotFoundUndoOperationCallException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    private ErrorDTO getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDTO().httpStatus(httpStatus.value()).message(message);
    }

    private void logException(Exception exception, HttpStatus status) {
        Tracer tracer = openTelemetry.getTracer(applicationName + ".exception-tracer");
        Span span = tracer.spanBuilder("exception-span").startSpan();
        span.setStatus(StatusCode.ERROR, String.format("%d: %s", status.value(), status.getReasonPhrase()));
        span.recordException(exception);
        String traceId = ObserverUtils.getTraceId(span);
        String spanId = ObserverUtils.getSpanId(span);
        String exceptionClassName = exception.getClass().getSimpleName();
        String exceptionMessage = exception.getMessage();
        String logMessage = String.format("[%s-%s] %s occurred at: %s", traceId, spanId, exceptionClassName, exceptionMessage);
        log.warn(logMessage);
        span.end();
    }
}
