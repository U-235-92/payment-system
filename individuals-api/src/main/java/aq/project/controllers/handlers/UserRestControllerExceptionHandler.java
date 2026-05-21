package aq.project.controllers.handlers;

import aq.project.controllers.UserRestController;
import aq.project.dto.ErrorDTO;
import aq.project.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = UserRestController.class)
public class UserRestControllerExceptionHandler {
    
//    Project specific exceptions
    @ExceptionHandler(value = IncorrectUserCredentialsException.class)
    public Mono<ResponseEntity<ErrorDTO>> onIncorrectUserCredentialsException(IncorrectUserCredentialsException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = UserExistsException.class)
    public Mono<ResponseEntity<ErrorDTO>> onUserExistsException(UserExistsException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public Mono<ResponseEntity<ErrorDTO>> onTokenExpirationException(InvalidTokenException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ServiceException.class)
    public Mono<ResponseEntity<ErrorDTO>> onServiceException(ServiceException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = InvalidPasswordConfirmException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidPasswordConfirmException(InvalidPasswordConfirmException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = InvalidAccessTokenException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidAccessTokenException(InvalidAccessTokenException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = InvalidUserRegistrationEventException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidUserRegistrationEventException(InvalidUserRegistrationEventException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = InvalidIndividualsDataException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidIndividualsDataException(InvalidIndividualsDataException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ExternalServiceException.class)
    public Mono<ResponseEntity<ErrorDTO>> onExternalServiceCallException(ExternalServiceException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    Non-project specific exceptions
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(ConstraintViolationException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<ResponseEntity<ErrorDTO>> onHttpMessageNotReadableException(HttpMessageNotReadableException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorDTO>> onIllegalArgumentException(IllegalArgumentException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }
}
