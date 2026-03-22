package aq.project.controllers.advices;

import aq.project.dto.ErrorDTO;
import aq.project.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GatewayUserRestControllerExceptionHandler {

    @ExceptionHandler(value = IncorrectUserCredentialsException.class)
    public Mono<ResponseEntity<ErrorDTO>> onIncorrectUserCredentialsException(IncorrectUserCredentialsException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = UserExistsException.class)
    public Mono<ResponseEntity<ErrorDTO>> onUserExistsException(UserExistsException exc) {
        HttpStatus status = HttpStatus.CONFLICT;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public Mono<ResponseEntity<ErrorDTO>> onTokenExpirationException(InvalidTokenException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = ServiceException.class)
    public Mono<ResponseEntity<ErrorDTO>> onServiceException(ServiceException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidPasswordConfirmException.class)
    public Mono<ResponseEntity<ErrorDTO>> onInvalidPasswordConfirmException(InvalidPasswordConfirmException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
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
        ErrorDTO errorDTO = getErrorDTO(status, msg + details);
        return Mono.just(ResponseEntity.status(status).body(errorDTO));
    }

    @ExceptionHandler(value = LackAccessTokenException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(LackAccessTokenException exc) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = InvalidUserRegistrationEventException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(InvalidUserRegistrationEventException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = LackIndividualsDataException.class)
    public Mono<ResponseEntity<ErrorDTO>> onLackIndividualsDataException(LackIndividualsDataException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(value = ExternalServiceException.class)
    public Mono<ResponseEntity<ErrorDTO>> onExternalServiceCallException(ExternalServiceException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorDTO errorDTO = getErrorDTO(status, exc.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> onHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    private ErrorDTO getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDTO(httpStatus.value(), message);
    }
}
