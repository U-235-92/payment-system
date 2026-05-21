package aq.project.controllers.handlers;

import aq.project.controllers.TransactionRestController;
import aq.project.dto.ErrorDTO;
import aq.project.exceptions.TransactionException;
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

//    Project specific exceptions
    @ExceptionHandler(value = TransactionException.class)
    public Mono<ResponseEntity<ErrorDTO>> onTransactionException(TransactionException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.valueOf(exc.getHttpStatusCode()));
    }

//    Non-project specific exceptions
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(ConstraintViolationException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorDTO>> onIllegalArgumentException(IllegalArgumentException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }
}
