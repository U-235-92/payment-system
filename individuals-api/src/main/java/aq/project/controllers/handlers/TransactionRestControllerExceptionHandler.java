package aq.project.controllers.handlers;

import aq.project.controllers.TransactionRestController;
import aq.project.dto.ErrorDTO;
import aq.project.exceptions.TransactionException;
import aq.project.util.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = TransactionRestController.class)
public class TransactionRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project specific exceptions
    @ExceptionHandler(value = TransactionException.class)
    public Mono<ResponseEntity<ErrorDTO>> onTransactionException(TransactionException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getHttpStatusCode());
        controllerExceptionLogger.logException(exc, status);
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

//    Non-project specific exceptions
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(ConstraintViolationException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(exc, status);
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorDTO>> onIllegalArgumentException(IllegalArgumentException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(exc, status);
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    private ErrorDTO getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDTO().httpStatus(httpStatus.value()).message(message);
    }
}
