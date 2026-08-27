package aq.project.controllers.handlers;

import aq.project.controllers.DepositTransactionRestController;
import aq.project.dto.ErrorDto;
import aq.project.exceptions.DuplicateTransactionHandleException;
import aq.project.exceptions.EntityConstraintsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.utils.logging.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = DepositTransactionRestController.class)
public class DepositTransactionRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers
    @ExceptionHandler(DuplicateTransactionHandleException.class)
    public ResponseEntity<ErrorDto> onDuplicateTransactionHandleException(DuplicateTransactionHandleException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(EntityConstraintsException.class)
    public ResponseEntity<ErrorDto> onEntityConstraintsException(EntityConstraintsException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> onIllegalStateException(IllegalStateException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    private ErrorDto getErrorDto(HttpStatus httpStatus, String message) {
        return new ErrorDto().httpStatus(httpStatus.value()).message(message);
    }
}
