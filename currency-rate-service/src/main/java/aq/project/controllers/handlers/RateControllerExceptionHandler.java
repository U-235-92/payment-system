package aq.project.controllers.handlers;

import aq.project.dto.CurrencyRateServiceErrorDto;
import aq.project.utils.logging.ControllerExceptionLogger;
import com.fasterxml.jackson.core.JsonParseException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class RateControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onJsonParseException(JsonParseException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    private ResponseEntity<CurrencyRateServiceErrorDto> getErrorResponseEntity(HttpStatus httpStatus, Exception e) {
        return ResponseEntity.status(httpStatus)
                .body(new CurrencyRateServiceErrorDto()
                        .httpStatus(httpStatus.value())
                        .message(e.getMessage()));
    }
}
