package aq.project.controllers.handlers;

import aq.project.controllers.PaymentRestController;
import aq.project.dto.ErrorDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = PaymentRestController.class)
public class PaymentRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project specific exceptions
    @ExceptionHandler(value = FallbackOperationException.class)
    public Mono<ResponseEntity<ErrorDto>> onFallbackOperationException(FallbackOperationException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(exc), status));
    }

//    Non-project specific exceptions
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorDto>> onConstraintViolationException(ConstraintViolationException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(exc), status));
    }

    @ExceptionHandler(value = IOException.class)
    public Mono<ResponseEntity<ErrorDto>> onIOException(IOException exc) {
        HttpStatus status = HttpStatus.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value());

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(exc), status));
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public Mono<ResponseEntity<ErrorDto>> onHttpClientErrorException(HttpClientErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(exc), status));
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    public Mono<ResponseEntity<ErrorDto>> onHttpServerErrorException(HttpServerErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(exc), status));
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public Mono<ResponseEntity<ErrorDto>> onIllegalStateException(IllegalStateException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(exc), status));
    }

    private ErrorDto getErrorDto(Exception e) {
        return ErrorDto.builder()
                .message(e.getMessage())
                .build();
    }
}