package aq.project.controllers.payment_controller.handlers;

import aq.project.controllers.payment_controller.PaymentServiceRestController;
import aq.project.dto.IndividualsApiServiceErrorDto;
import aq.project.exceptions.FallbackOperationException;
import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = PaymentServiceRestController.class)
public class PaymentServiceRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

//    Project specific exceptions
    @ExceptionHandler(value = FallbackOperationException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onFallbackOperationException(FallbackOperationException exc) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(exc, status);

        IndividualsApiServiceErrorDto dto = getIndividualsApiServiceErrorDto(status, exc);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(dto, status));
    }

//    Non-project specific exceptions
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onConstraintViolationException(ConstraintViolationException exc) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(exc, status);

        IndividualsApiServiceErrorDto dto = getIndividualsApiServiceErrorDto(status, exc);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(dto, status));
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onHttpClientErrorException(HttpClientErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());

        controllerExceptionLogger.logException(exc, status);

        IndividualsApiServiceErrorDto dto = getIndividualsApiServiceErrorDto(status, exc);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(dto, status));
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    public Mono<ResponseEntity<IndividualsApiServiceErrorDto>> onHttpServerErrorException(HttpServerErrorException exc) {
        HttpStatus status = HttpStatus.valueOf(exc.getStatusCode().value());

        controllerExceptionLogger.logException(exc, status);

        IndividualsApiServiceErrorDto dto = getIndividualsApiServiceErrorDto(status, exc);

        return Mono.just(ErrorResponseEntityFactory.getErrorResponseEntity(dto, status));
    }

    private IndividualsApiServiceErrorDto getIndividualsApiServiceErrorDto(HttpStatus httpStatus, Exception e) {
        IndividualsApiServiceErrorDto dto = new IndividualsApiServiceErrorDto();
        dto.setHttpStatus(httpStatus.value());
        dto.setMessage(e.getMessage());
        return dto;
    }
}
