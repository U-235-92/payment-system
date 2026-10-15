package aq.project.controllers.handlers;

import aq.project.dto.PaymentProviderServiceErrorDto;
import aq.project.utils.logging.ControllerExceptionLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static aq.project.controllers._utils._ErrorResponseEntityFactory.getErrorResponseEntity;

@RestControllerAdvice
@RequiredArgsConstructor
public class UsernameNotFoundExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<PaymentProviderServiceErrorDto> onUsernameNotFoundException(UsernameNotFoundException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        
        controllerExceptionLogger.logException(e, status);

        return getErrorResponseEntity(status, e);
    }
}
