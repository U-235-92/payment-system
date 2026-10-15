package aq.project.controllers.wallet_controller.handlers;

import aq.project.controllers.wallet_controller.WalletRestController;
import aq.project.dto.IndividualsApiServiceErrorDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.exceptions.WalletException;
import aq.project.utils.logging.ControllerExceptionLogger;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Mono;

import static aq.project.controllers._utils._ErrorResponseEntityFactory.getErrorResponseEntity;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = WalletRestController.class)
public class WalletRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project specific exceptions
    @ExceptionHandler(value = WalletException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onWalletException(WalletException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getHttpStatusCode());

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = FallbackOperationException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onFallbackOperationException(FallbackOperationException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

//    Non-project specific exceptions
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onConstraintViolationException(ConstraintViolationException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onIllegalArgumentException(IllegalArgumentException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onHttpClientErrorException(HttpClientErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onHttpServerErrorException(HttpServerErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());

        controllerExceptionLogger.logException(exc, status);

        return Mono.just(getErrorResponseEntity(status, exc));
    }
}
