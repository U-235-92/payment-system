package aq.project.controllers.handlers;

import aq.project.controllers.AdminRestController;
import aq.project.dto.ErrorDto;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.utils.logging.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = { AdminRestController.class })
public class AdminRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ErrorDto> onEntityAlreadyExistsException(EntityAlreadyExistsException e) {
        HttpStatus status = HttpStatus.CONFLICT;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDto> onUsernameNotFoundException(UsernameNotFoundException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDto> onAuthenticationException(AuthenticationException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status).body(getErrorDTO(status, e.getMessage()));
    }

    private ErrorDto getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDto().httpStatus(httpStatus.value()).message(message);
    }
}
