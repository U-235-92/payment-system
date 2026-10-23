package aq.project.controllers.handlers;

import aq.project.dto.PersonServiceErrorDto;
import aq.project.exceptions.EntityAlreadyExistException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.utils.logging.ControllerExceptionLogger;
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

//    Project specific exceptions
    @ExceptionHandler(EntityAlreadyExistException.class)
    public ResponseEntity<PersonServiceErrorDto> onEntityAlreadyExistException(EntityAlreadyExistException e) {
        HttpStatus status = HttpStatus.CONFLICT;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<PersonServiceErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(ProcessOperationException.class)
    public ResponseEntity<PersonServiceErrorDto> onProcessOperationException(ProcessOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

//    Non project specific exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PersonServiceErrorDto> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<PersonServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<PersonServiceErrorDto> onMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<PersonServiceErrorDto> onHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PersonServiceErrorDto> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<PersonServiceErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    private ResponseEntity<PersonServiceErrorDto> getErrorResponseEntity(HttpStatus status, Exception e) {
        return ResponseEntity.status(status)
                .body(new PersonServiceErrorDto()
                        .httpStatus(status.value())
                        .message(e.getMessage()));
    }
}
