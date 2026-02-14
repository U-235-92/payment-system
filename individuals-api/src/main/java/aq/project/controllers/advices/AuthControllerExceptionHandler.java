package aq.project.controllers.advices;

import aq.project.dto.ErrorDTO;
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
    public Mono<ResponseEntity<ErrorDTO>> onIncorrectUserCredentialsException(IncorrectUserCredentialsException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage(), exc);
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = UserExistsException.class)
    public Mono<ResponseEntity<ErrorDTO>> onUserExistsException(UserExistsException exc) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage(), exc);
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public Mono<ResponseEntity<ErrorDTO>> onTokenExpirationException(InvalidTokenException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage(), exc);
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = ServiceException.class)
    public Mono<ResponseEntity<ErrorDTO>> onServiceException(ServiceException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage(), exc);
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidPasswordConfirmException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidPasswordConfirmException(InvalidPasswordConfirmException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage(), exc);
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(ConstraintViolationException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Attempt of registration an user with wrong user data: ";
        String details = exc.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath().toString() + " = " + violation.getInvalidValue())
                .collect(Collectors.joining("; "));
        ErrorDTO errorDTO = getErrorDTO(status, msg + details, exc);
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = LackAccessTokenException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(LackAccessTokenException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage(), exc);
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidUserRegistrationRequestException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(InvalidUserRegistrationRequestException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage(), exc);
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    private ErrorDTO getErrorDTO(HttpStatus httpStatus, String message, Exception exception) {
        return new ErrorDTO(exception.getClass(), httpStatus.value(), message);
    }
}
