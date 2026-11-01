package aq.project.controllers.handlers;

import aq.project.controllers.WithdrawTransactionRestController;
import aq.project.dto.TransactionServiceErrorDto;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
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
@RestControllerAdvice(basePackageClasses = { WithdrawTransactionRestController.class })
public class WithdrawTransactionRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers
    @ExceptionHandler(ProcessOperationException.class)
    public ResponseEntity<TransactionServiceErrorDto> onProcessOperationException(ProcessOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        TransactionServiceErrorDto dto = getTransactionServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<TransactionServiceErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        TransactionServiceErrorDto dto = getTransactionServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(FallbackOperationException.class)
    public ResponseEntity<TransactionServiceErrorDto> onFallbackOperationException(FallbackOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        TransactionServiceErrorDto dto = getTransactionServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<TransactionServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        TransactionServiceErrorDto dto = getTransactionServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TransactionServiceErrorDto> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        TransactionServiceErrorDto dto = getTransactionServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<TransactionServiceErrorDto> onExecutionException(ExecutionException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        TransactionServiceErrorDto dto = getTransactionServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<TransactionServiceErrorDto> onInterruptedException(InterruptedException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        TransactionServiceErrorDto dto = getTransactionServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<TransactionServiceErrorDto> onIllegalStateException(IllegalStateException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        TransactionServiceErrorDto dto = getTransactionServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    private TransactionServiceErrorDto getTransactionServiceErrorDto(HttpStatus httpStatus, Exception e) {
        TransactionServiceErrorDto dto = new TransactionServiceErrorDto();
        dto.setHttpStatus(httpStatus.value());
        dto.setMessage(e.getMessage());
        return dto;
    }
}
