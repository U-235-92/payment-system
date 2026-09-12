package aq.project.controllers.handlers;

import aq.project.controllers.UserRestController;
import aq.project.dto.ErrorDto;
import aq.project.exceptions.*;
import aq.project.utils.logging.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = UserRestController.class)
public class UserRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project specific exceptions
    @ExceptionHandler(value = IncorrectUserCredentialsException.class)
    public Mono<ResponseEntity<ErrorDto>> onIncorrectUserCredentialsException(IncorrectUserCredentialsException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = UserExistsException.class)
    public Mono<ResponseEntity<ErrorDto>> onUserExistsException(UserExistsException exc) {
        HttpStatus status = HttpStatus.CONFLICT;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public Mono<ResponseEntity<ErrorDto>> onTokenExpirationException(InvalidTokenException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = ServiceException.class)
    public Mono<ResponseEntity<ErrorDto>> onServiceException(ServiceException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = InvalidPasswordConfirmException.class)
    public Mono<ResponseEntity<ErrorDto>> onInvalidPasswordConfirmException(InvalidPasswordConfirmException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = InvalidAccessTokenException.class)
    public Mono<ResponseEntity<ErrorDto>> onInvalidAccessTokenException(InvalidAccessTokenException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = InvalidUserRegistrationEventException.class)
    public Mono<ResponseEntity<ErrorDto>> onInvalidUserRegistrationEventException(InvalidUserRegistrationEventException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = InvalidIndividualsDataException.class)
    public Mono<ResponseEntity<ErrorDto>> onInvalidIndividualsDataException(InvalidIndividualsDataException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = ExternalServiceException.class)
    public Mono<ResponseEntity<ErrorDto>> onExternalServiceCallException(ExternalServiceException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<ResponseEntity<ErrorDto>> onHttpMessageNotReadableException(HttpMessageNotReadableException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(exc, status);
        ErrorDto errorDTO = getErrorDto(status, exc.getMessage());
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(IllegalArgumentException.class)
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
