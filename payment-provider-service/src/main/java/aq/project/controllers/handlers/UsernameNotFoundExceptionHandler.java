package aq.project.controllers.handlers;

import aq.project.dto.ErrorDto;
import aq.project.utils.logging.ControllerExceptionLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class UsernameNotFoundExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDto> onUsernameNotFoundException(UsernameNotFoundException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        controllerExceptionLogger.logException(e, status);
        ErrorDto errorDto = new ErrorDto().httpStatus(status.value()).message(e.getMessage());
        return ResponseEntity.status(status).body(errorDto);
    }
}
