package aq.project.controllers.handlers;

import aq.project.controllers.WalletRestController;
import aq.project.dto.ErrorDTO;
import aq.project.exceptions.WalletException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = WalletRestController.class)
public class WalletRestControllerExceptionHandler {

//    Project specific exceptions
    @ExceptionHandler(value = WalletException.class)
    public Mono<ResponseEntity<ErrorDTO>> onWalletException(WalletException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.valueOf(exc.getHttpStatusCode()));
    }

//    Non-project specific exceptions
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorDTO>> onConstraintViolationException(ConstraintViolationException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorDTO>> onIllegalArgumentException(IllegalArgumentException exc) {
        return ExceptionHandlerUtil.getErrorResponse(exc, HttpStatus.BAD_REQUEST);
    }
}
