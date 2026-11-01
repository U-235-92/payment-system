package aq.project.controllers.handlers;

import aq.project.controllers.TransactionRestController;
import aq.project.dto.PaymentProviderServiceErrorDto;
import aq.project.exceptions.AbsentPropertyException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.ForeignMerchantException;
import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = { TransactionRestController.class })
public class TransactionRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(ForeignMerchantException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onForeignMerchantTransactionException(ForeignMerchantException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(AbsentPropertyException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onAbsentPropertyException(AbsentPropertyException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

//    Non project's exception handlers
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onRuntimeException(RuntimeException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onUsernameNotFoundException(UsernameNotFoundException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        
        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onAuthenticationException(AuthenticationException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        
        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    private PaymentProviderServiceErrorDto getPaymentProviderServiceErrorDto(HttpStatus httpStatus, Exception e) {
        PaymentProviderServiceErrorDto dto = new PaymentProviderServiceErrorDto();
        dto.setHttpStatus(httpStatus.value());
        dto.setMessage(e.getMessage());
        return dto;
    }
}
