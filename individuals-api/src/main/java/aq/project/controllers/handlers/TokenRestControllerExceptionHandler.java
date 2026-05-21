package aq.project.controllers.handlers;

import aq.project.controllers.TokenRestController;
import aq.project.dto.ErrorDTO;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = TokenRestController.class)
public class TokenRestControllerExceptionHandler {

//    Project specific exceptions

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
