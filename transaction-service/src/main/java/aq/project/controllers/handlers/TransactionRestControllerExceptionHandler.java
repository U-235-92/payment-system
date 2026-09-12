package aq.project.controllers.handlers;

import aq.project.controllers.TransactionRestController;
import aq.project.dto.ErrorDto;
import aq.project.exceptions.*;
import aq.project.utils.logging.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = { TransactionRestController.class })
public class TransactionRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers [aq.project.exceptions.*]
    @ExceptionHandler(UnknownMessagePropertyException.class)
    public ResponseEntity<ErrorDto> onUnknownMessagePropertyException(UnknownMessagePropertyException e) {
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

    @ExceptionHandler(ServiceHttpException.class)
    public ResponseEntity<ErrorDto> onServiceHttpException(ServiceHttpException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(ClientHttpException.class)
    public ResponseEntity<ErrorDto> onClientHttpException(ClientHttpException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ErrorDto> onTransactionException(TransactionException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(ExceedAttemptLimitException.class)
    public ResponseEntity<ErrorDto> onExceedAttemptLimitException(ExceedAttemptLimitException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(FallbackOperationException.class)
    public ResponseEntity<ErrorDto> onFallbackOperationException(FallbackOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
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

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<ErrorDto> onExecutionException(ExecutionException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ErrorDto> onInterruptedException(InterruptedException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> onIllegalStateException(IllegalStateException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDto(status, e.getMessage()));
    }

    private ErrorDto getErrorDto(HttpStatus httpStatus, String message) {
        return new ErrorDto().httpStatus(httpStatus.value()).message(message);
    }
}
