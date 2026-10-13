package aq.project.controllers.handlers;

import aq.project.controllers.wallet.WalletRestController;
import aq.project.dto.WalletServiceErrorDto;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.utils.logging.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static aq.project.controllers.handlers._ErrorResponseEntityFactory.getErrorResponseEntity;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = WalletRestController.class)
public class WalletRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<WalletServiceErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

//    Non project's exception handlers
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<WalletServiceErrorDto> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<WalletServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<WalletServiceErrorDto> onIllegalArgumentException(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WalletServiceErrorDto> onException(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }
}
