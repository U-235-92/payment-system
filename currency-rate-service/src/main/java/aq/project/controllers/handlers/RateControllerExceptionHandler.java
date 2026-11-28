package aq.project.controllers.handlers;

import aq.project.dto.ErrorDto;
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
    public ResponseEntity<ErrorDto> onFallbackOperationException(FallbackOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<ErrorDto> onJsonParseException(JsonParseException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    private ErrorDto getErrorDto(Exception e) {
        return ErrorDto.builder()
                .message(e.getMessage())
                .build();
    }
}