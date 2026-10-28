package aq.project.controllers.handlers;

import aq.project.controllers.TransactionRestController;
import aq.project.dto.PaymentProviderServiceErrorDto;
import aq.project.exceptions.AbsentPropertyException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantException;
import aq.project.utils.logging.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static aq.project.controllers._utils._ErrorResponseEntityFactory.getErrorResponseEntity;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = { TransactionRestController.class })
public class TransactionRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(ForeignMerchantException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onForeignMerchantTransactionException(ForeignMerchantException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(AbsentPropertyException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onAbsentPropertyException(AbsentPropertyException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onUsernameNotFoundException(UsernameNotFoundException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        
        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onAuthenticationException(AuthenticationException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        
        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }
}
