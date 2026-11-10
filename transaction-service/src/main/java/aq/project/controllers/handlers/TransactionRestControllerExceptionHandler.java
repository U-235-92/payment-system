package aq.project.controllers.handlers;

import aq.project.dto.TransactionServiceErrorDto;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class TransactionRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<TransactionServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<TransactionServiceErrorDto> onIllegalStateException(IllegalStateException e) {
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

    private TransactionServiceErrorDto getTransactionServiceErrorDto(HttpStatus httpStatus, Exception e) {
        TransactionServiceErrorDto dto = new TransactionServiceErrorDto();
        dto.setHttpStatus(httpStatus.value());
        dto.setMessage(e.getMessage());
        return dto;
    }
}
