package aq.project.controllers.handlers;

import aq.project.dto.ErrorDto;
import aq.project.exceptions.EntityAlreadyExistException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class PersonRestControllerExceptionHandler {
    
    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project specific exceptions
    @ExceptionHandler(EntityAlreadyExistException.class)
    public ResponseEntity<ErrorDto> onEntityAlreadyExistException(EntityAlreadyExistException e) {
        HttpStatus status = HttpStatus.CONFLICT;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(ProcessOperationException.class)
    public ResponseEntity<ErrorDto> onProcessOperationException(ProcessOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

//    Non project specific exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> onHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return ErrorResponseEntityFactory.getErrorResponseEntity(getErrorDto(e), status);
    }

    private ErrorDto getErrorDto(Exception e) {
        return ErrorDto.builder()
                .message(e.getMessage())
                .build();
    }
}
