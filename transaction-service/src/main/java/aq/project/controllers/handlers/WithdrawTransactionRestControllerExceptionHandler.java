package aq.project.controllers.handlers;

import aq.project.controllers.WithdrawTransactionRestController;
import aq.project.dto.TransactionServiceErrorDto;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.exceptions.ProcessOperationException;
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

import static aq.project.controllers._utils._ErrorResponseEntityFactory.getErrorResponseEntity;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = { WithdrawTransactionRestController.class })
public class WithdrawTransactionRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers
    @ExceptionHandler(ProcessOperationException.class)
    public ResponseEntity<TransactionServiceErrorDto> onProcessOperationException(ProcessOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<TransactionServiceErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(FallbackOperationException.class)
    public ResponseEntity<TransactionServiceErrorDto> onFallbackOperationException(FallbackOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<TransactionServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TransactionServiceErrorDto> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<TransactionServiceErrorDto> onExecutionException(ExecutionException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<TransactionServiceErrorDto> onInterruptedException(InterruptedException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<TransactionServiceErrorDto> onIllegalStateException(IllegalStateException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }
}
