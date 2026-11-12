package aq.project.controllers.handlers;

import aq.project.dto.FakePaymentProviderServiceErrorDto;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.exceptions.EntityNotFoundException;
import aq.project.exceptions.FallbackOperationException;
import aq.project.exceptions.NotEqualsEntityException;
import aq.project.utils.logging.ControllerExceptionLogger;
import aq.project.utils.rest_controller_error_handle.ErrorResponseEntityFactory;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CommonRestControllerExceptionHandler {

    private final ControllerExceptionLogger controllerExceptionLogger;

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<FakePaymentProviderServiceErrorDto> onConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        FakePaymentProviderServiceErrorDto dto = getFakePaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<FakePaymentProviderServiceErrorDto> onEntityNotFoundException(EntityNotFoundException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        controllerExceptionLogger.logException(e, status);

        FakePaymentProviderServiceErrorDto dto = getFakePaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<FakePaymentProviderServiceErrorDto> onEntityAlreadyExistsException(EntityAlreadyExistsException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        FakePaymentProviderServiceErrorDto dto = getFakePaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(NotEqualsEntityException.class)
    public ResponseEntity<FakePaymentProviderServiceErrorDto> onNotEqualsEntityException(NotEqualsEntityException e) {
        HttpStatus status = HttpStatus.FORBIDDEN;

        controllerExceptionLogger.logException(e, status);

        FakePaymentProviderServiceErrorDto dto = getFakePaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<FakePaymentProviderServiceErrorDto> onIllegalStateException(IllegalStateException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        controllerExceptionLogger.logException(e, status);

        FakePaymentProviderServiceErrorDto dto = getFakePaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<FakePaymentProviderServiceErrorDto> onNullPointerException(NullPointerException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        FakePaymentProviderServiceErrorDto dto = getFakePaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    @ExceptionHandler(FallbackOperationException.class)
    public ResponseEntity<FakePaymentProviderServiceErrorDto> onFallbackOperationException(FallbackOperationException e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        controllerExceptionLogger.logException(e, status);

        FakePaymentProviderServiceErrorDto dto = getFakePaymentProviderServiceErrorDto(status, e);

        return ErrorResponseEntityFactory.getErrorResponseEntity(dto, status);
    }

    private FakePaymentProviderServiceErrorDto getFakePaymentProviderServiceErrorDto(HttpStatus httpStatus, Exception e) {
        FakePaymentProviderServiceErrorDto dto = new FakePaymentProviderServiceErrorDto();
        dto.setHttpStatus(httpStatus.value());
        dto.setMessage(e.getMessage());
        return dto;
    }
}
