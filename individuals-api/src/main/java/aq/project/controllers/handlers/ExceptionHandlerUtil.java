package aq.project.controllers.handlers;

import aq.project.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

abstract class ExceptionHandlerUtil {

    protected static Mono<ResponseEntity<ErrorDTO>> getErrorResponse(Exception e, HttpStatus status) {
        ErrorDTO errorDTO = getErrorDTO(status, e.getMessage());
        ResponseEntity<ErrorDTO> response = ResponseEntity.status(status).body(errorDTO);
        return Mono.just(response);
    }

    private static ErrorDTO getErrorDTO(HttpStatus httpStatus, String message) {
        return new ErrorDTO().httpStatus(httpStatus.value()).message(message);
    }
}
