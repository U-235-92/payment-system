package aq.project.controllers.advices;

import aq.project.dto.ErrorDTO;
import aq.project.exceptions.*;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import static aq.project.util.telemetry.TelemetryUtils.*;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GatewayUserRestControllerExceptionHandler {

    @Value("${spring.application.name}")
    private String applicationName;

    private final OpenTelemetry openTelemetry;

    @ExceptionHandler(value = IncorrectUserCredentialsException.class)
    public Mono<ResponseEntity<ErrorDTO>> onIncorrectUserCredentialsException(IncorrectUserCredentialsException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        logException(exc);
        return Mono.just(response);
    }

    @ExceptionHandler(value = UserExistsException.class)
    public Mono<ResponseEntity<ErrorDTO>> onUserExistsException(UserExistsException exc) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        logException(exc);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public Mono<ResponseEntity<ErrorDTO>> onTokenExpirationException(InvalidTokenException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        logException(exc);
        return Mono.just(response);
    }

    @ExceptionHandler(value = ServiceException.class)
    public Mono<ResponseEntity<ErrorDTO>> onServiceException(ServiceException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        logException(exc);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidPasswordConfirmException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidPasswordConfirmException(InvalidPasswordConfirmException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        logException(exc);
        return Mono.just(response);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(ConstraintViolationException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Attempt of registration an user with wrong user data: ";
        String details = exc.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath().toString() + " = " + violation.getInvalidValue())
                .collect(Collectors.joining("; "));
        ErrorDTO errorDTO = getErrorDTO(status, msg + details);
        logException(exc);
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = InvalidAccessTokenException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidAccessTokenException(InvalidAccessTokenException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        logException(exc);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidUserRegistrationEventException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidUserRegistrationEventException(InvalidUserRegistrationEventException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        logException(exc);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidIndividualsDataException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidIndividualsDataException(InvalidIndividualsDataException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        logException(exc);
        return Mono.just(response);
    }

    @ExceptionHandler(value = ExternalServiceException.class)
    public Mono<ResponseEntity<ErrorDTO>> onExternalServiceCallException(ExternalServiceException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        logException(exc);
        return Mono.just(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> onHttpMessageNotReadableException(HttpMessageNotReadableException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(exc);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, exc.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> onIllegalArgumentException(IllegalArgumentException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logException(exc);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, exc.getMessage()));
    }

    private ErrorDTO getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDTO(httpStatus.value(), message);
    }

    private void logException(Exception exception) {
        Tracer tracer = getTracer(applicationName, "gateway-user-rest-controller-exception-handler-tracer", openTelemetry);
        Span span = getSpan(tracer, "exception-span");
        span.recordException(exception);
        String logMessage = String.format("[%s-%s] %s occurred at: %s", getTraceId(span), getSpanId(span), exception.getClass().getName(), exception.getMessage());
        log.warn(logMessage);
        span.end();
    }
}
