package aq.project.controllers.handlers;

import aq.project.controllers.PaymentRestController;
import aq.project.dto.ErrorDto;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.exceptions.ProhibitedOperationException;
import aq.project.utils.logging.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = { PaymentRestController.class })
public class PaymentRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(ProhibitedOperationException.class)
    public ResponseEntity<ErrorDto> onProhibitedOperationException(ProhibitedOperationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(FallbackOperationException.class)
    public ResponseEntity<ErrorDto> onFallbackOperationException(FallbackOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    private ErrorDto getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDto().httpStatus(httpStatus.value()).message(message);
    }
}
