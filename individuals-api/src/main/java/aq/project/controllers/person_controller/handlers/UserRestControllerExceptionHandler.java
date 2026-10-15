package aq.project.controllers.person_controller.handlers;

import aq.project.controllers.person_controller.UserRestController;
import aq.project.dto.IndividualsApiServiceErrorDto;
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

import static aq.project.controllers._utils._ErrorResponseEntityFactory.getErrorResponseEntity;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = UserRestController.class)
public class UserRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project specific exceptions
    @ExceptionHandler(value = IncorrectUserCredentialsException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onIncorrectUserCredentialsException(IncorrectUserCredentialsException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = UserExistsException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onUserExistsException(UserExistsException exc) {
        HttpStatus status = HttpStatus.CONFLICT;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onTokenExpirationException(InvalidTokenException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = ServiceException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onServiceException(ServiceException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = InvalidPasswordConfirmException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onInvalidPasswordConfirmException(InvalidPasswordConfirmException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = InvalidAccessTokenException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onInvalidAccessTokenException(InvalidAccessTokenException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = InvalidUserRegistrationEventException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onInvalidUserRegistrationEventException(InvalidUserRegistrationEventException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = InvalidIndividualsDataException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onInvalidIndividualsDataException(InvalidIndividualsDataException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = ExternalServiceException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onExternalServiceCallException(ExternalServiceException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

//    Non-project specific exceptions
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onConstraintViolationException(ConstraintViolationException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onHttpMessageNotReadableException(HttpMessageNotReadableException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onIllegalArgumentException(IllegalArgumentException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onHttpClientErrorException(HttpClientErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onHttpServerErrorException(HttpServerErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }
}
