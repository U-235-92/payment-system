package aq.project.controllers.advices;

import aq.project.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice
public class AuthControllerExceptionHandler {

    @ExceptionHandler(value = IncorrectUserCredentialsException.class)
    public Mono<ResponseEntity<String>> onIncorrectUserCredentialsException(IncorrectUserCredentialsException exc) {
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exc.getMessage());
        return Mono.just(response);
    }

    @ExceptionHandler(value = UserExistsException.class)
    public Mono<ResponseEntity<String>> onUserExistsException(UserExistsException exc) {
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.CONFLICT).body(exc.getMessage());
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public Mono<ResponseEntity<String>> onTokenExpirationException(InvalidTokenException exc) {
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exc.getMessage());
        return Mono.just(response);
    }

    @ExceptionHandler(value = ServiceException.class)
    public Mono<ResponseEntity<String>> onServiceException(ServiceException exc) {
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exc.getMessage());
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidPasswordConfirmException.class)
    public Mono<ResponseEntity<String>> onInvalidPasswordConfirmException(InvalidPasswordConfirmException exc) {
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exc.getMessage());
        return Mono.just(response);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<String>> onConstraintViolationException(ConstraintViolationException exc) {
        String msg = "Attempt of registration an user with wrong user data: ";
        String details = exc.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath().toString() + " = " + violation.getInvalidValue())
                .collect(Collectors.joining("; "));
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg + details));
    }

    @ExceptionHandler(value = LackAccessTokenException.class)
    public Mono<ResponseEntity<String>> onConstraintViolationException(LackAccessTokenException exc) {
        ResponseEntity<String> response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exc.getMessage());
        return Mono.just(response);
    }
}
