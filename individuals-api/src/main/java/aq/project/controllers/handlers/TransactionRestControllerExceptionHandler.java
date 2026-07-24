package aq.project.controllers.handlers;

import aq.project.controllers.TransactionRestController;
import aq.project.dto.ErrorDto;
import aq.project.exceptions.TransactionException;
import aq.project.utils.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = TransactionRestController.class)
public class TransactionRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project specific exceptions
    @ExceptionHandler(value = TransactionException.class)
    public Mono<ResponseEntity<ErrorDto>> onTransactionException(TransactionException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getHttpStatusCode());
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

//    Non-project specific exceptions
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorDto>> onConstraintViolationException(ConstraintViolationException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorDto>> onIllegalArgumentException(IllegalArgumentException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public Mono<ResponseEntity<ErrorDto>> onHttpClientErrorException(HttpClientErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    public Mono<ResponseEntity<ErrorDto>> onHttpServerErrorException(HttpServerErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    private ErrorDto getErrorDto(HttpStatus httpStatus, String message) {
        return new ErrorDto().httpStatus(httpStatus.value()).message(message);
    }
}
