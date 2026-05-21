package aq.project.controllers.handlers;

import aq.project.dto.ErrorDTO;
import aq.project.exceptions.OutboxEventException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class TransactionRestControllerExceptionHandler {

    private final ExceptionLogger exceptionLogger;

//    Project's exception handlers [aq.project.exceptions.*]
    @ExceptionHandler(OutboxEventException.class)
    public ResponseEntity<ErrorDTO> onOutboxEventException(OutboxEventException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    private ErrorDTO getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDTO().httpStatus(httpStatus.value()).message(message);
    }
}
