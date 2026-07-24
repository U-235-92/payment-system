package aq.project.controllers.handlers;

import aq.project.dto.ErrorDto;
import aq.project.dto.ErrorDto;
import aq.project.exceptions.*;
import aq.project.utils.ControllerExceptionLogger;
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
    public ResponseEntity<ErrorDto> onUserExistsException(UserExistsException e) {
        HttpStatus status = HttpStatus.CONFLICT;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(UserNotExistsException.class)
    public ResponseEntity<ErrorDto> onUserNotExistsException(UserNotExistsException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(CountryNotExistsException.class)
    public ResponseEntity<ErrorDto> onCountryNotExistsException(CountryNotExistsException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDto> onMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> onHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(NotFoundRevisionException.class)
    public ResponseEntity<ErrorDto> onNotFoundRevisionException(NotFoundRevisionException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(IllegalUndoOperationPayloadPropertyException.class)
    public ResponseEntity<ErrorDto> onIllegalUndoOperationPayloadPropertyException(IllegalUndoOperationPayloadPropertyException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(NotExpectedUndoOperationCallException.class)
    public ResponseEntity<ErrorDto> onNotExpectedUndoOperationCallException(NotExpectedUndoOperationCallException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    @ExceptionHandler(NotFoundUndoOperationCallException.class)
    public ResponseEntity<ErrorDto> onNotFoundUndoOperationCallException(NotFoundUndoOperationCallException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        controllerExceptionLogger.logException(e, status);
        return ResponseEntity.status(status.value()).body(getErrorDto(status, e.getMessage()));
    }

    private ErrorDto getErrorDto(HttpStatus httpStatus, String message) {
        return new ErrorDto().httpStatus(httpStatus.value()).message(message);
    }
}
