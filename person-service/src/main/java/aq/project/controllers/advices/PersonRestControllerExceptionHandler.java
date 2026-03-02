package aq.project.controllers.advices;

import aq.project.dto.ErrorDTO;
import aq.project.exceptions.CountryNotExistsException;
import aq.project.exceptions.UserExistsException;
import aq.project.exceptions.UserNotExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PersonRestControllerExceptionHandler {

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorDTO> onUserExistsException(UserExistsException e) {
        HttpStatus conflict = HttpStatus.CONFLICT;
        return ResponseEntity.status(conflict.value()).body(getErrorDTO(e, conflict, e.getMessage()));
    }

    @ExceptionHandler(UserNotExistsException.class)
    public ResponseEntity<ErrorDTO> onUserNotExistsException(UserNotExistsException e) {
        HttpStatus conflict = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(conflict.value()).body(getErrorDTO(e, conflict, e.getMessage()));
    }

    @ExceptionHandler(CountryNotExistsException.class)
    public ResponseEntity<ErrorDTO> onCountryNotExistsException(CountryNotExistsException e) {
        HttpStatus conflict = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(conflict.value()).body(getErrorDTO(e, conflict, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus conflict = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(conflict.value()).body(getErrorDTO(e, conflict, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus conflict = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(conflict.value()).body(getErrorDTO(e, conflict, e.getMessage()));
    }

    private ErrorDTO getErrorDTO(Exception exception, HttpStatus httpStatus, String message) {
        return new ErrorDTO(exception.getClass(), httpStatus.value(), message);
    }
}
