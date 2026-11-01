package aq.project.controllers.handlers;

import aq.project.dto.PaymentProviderServiceErrorDto;
import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
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
    public ResponseEntity<PaymentProviderServiceErrorDto> onUsernameNotFoundException(UsernameNotFoundException e) {
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
