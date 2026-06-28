package aq.project.controllers.handlers;

import aq.project.dto.ErrorDTO;
import aq.project.exceptions.*;
import aq.project.util.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class PersonRestControllerExceptionHandler {
    
    private final ControllerExceptionLogger controllerExceptionLogger;

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorDTO> onUserExistsException(UserExistsException e) {
        HttpStatus status = HttpStatus.CONFLICT;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(UserNotExistsException.class)
    public ResponseEntity<ErrorDTO> onUserNotExistsException(UserNotExistsException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(CountryNotExistsException.class)
    public ResponseEntity<ErrorDTO> onCountryNotExistsException(CountryNotExistsException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDTO> onMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> onHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(NotFoundRevisionException.class)
    public ResponseEntity<ErrorDTO> onNotFoundRevisionException(NotFoundRevisionException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalUndoOperationPayloadPropertyException.class)
    public ResponseEntity<ErrorDTO> onIllegalUndoOperationPayloadPropertyException(IllegalUndoOperationPayloadPropertyException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDTO> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(NotExpectedUndoOperationCallException.class)
    public ResponseEntity<ErrorDTO> onNotExpectedUndoOperationCallException(NotExpectedUndoOperationCallException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    @ExceptionHandler(NotFoundUndoOperationCallException.class)
    public ResponseEntity<ErrorDTO> onNotFoundUndoOperationCallException(NotFoundUndoOperationCallException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDTO(status, e.getMessage()));
    }

    private ErrorDTO getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDTO().httpStatus(httpStatus.value()).message(message);
    }
}
