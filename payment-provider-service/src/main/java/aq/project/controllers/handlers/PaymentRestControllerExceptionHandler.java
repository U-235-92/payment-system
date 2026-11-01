package aq.project.controllers.handlers;

import aq.project.controllers.PaymentRestController;
import aq.project.dto.PaymentProviderServiceErrorDto;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.exceptions.ProcessOperationException;
import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = { PaymentRestController.class })
public class PaymentRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project's exception handlers
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onEntityAlreadyExistsException(EntityAlreadyExistsException e) {
        HttpStatus status = HttpStatus.CONFLICT;

        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(ProcessOperationException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onProhibitedOperationException(ProcessOperationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        PaymentProviderServiceErrorDto dto = getPaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(FallbackOperationException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onFallbackOperationException(FallbackOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

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

    private PaymentProviderServiceErrorDto getPaymentProviderServiceErrorDto(HttpStatus httpStatus, Exception e) {
        PaymentProviderServiceErrorDto dto = new PaymentProviderServiceErrorDto();
        dto.setHttpStatus(httpStatus.value());
        dto.setMessage(e.getMessage());
        return dto;
    }
}
