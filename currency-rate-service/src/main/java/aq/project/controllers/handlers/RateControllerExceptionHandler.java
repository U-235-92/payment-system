package aq.project.controllers.handlers;

import aq.project.dto.CurrencyRateServiceErrorDto;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
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

//    Project's exception handlers
    @ExceptionHandler(FallbackOperationException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onFallbackOperationException(FallbackOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        CurrencyRateServiceErrorDto dto = getCurrencyRateServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        CurrencyRateServiceErrorDto dto = getCurrencyRateServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        CurrencyRateServiceErrorDto dto = getCurrencyRateServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        CurrencyRateServiceErrorDto dto = getCurrencyRateServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onJsonParseException(JsonParseException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        CurrencyRateServiceErrorDto dto = getCurrencyRateServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CurrencyRateServiceErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        CurrencyRateServiceErrorDto dto = getCurrencyRateServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    private CurrencyRateServiceErrorDto getCurrencyRateServiceErrorDto(HttpStatus httpStatus, Exception e) {
        CurrencyRateServiceErrorDto dto = new CurrencyRateServiceErrorDto();
        dto.setHttpStatus(httpStatus.value());
        dto.setMessage(e.getMessage());
        return dto;
    }
}
