package aq.project.controllers._utils;

import aq.project.dto.IndividualsApiServiceErrorDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class _ErrorResponseEntityFactory {

    public static ResponseEntity<IndividualsApiServiceErrorDto> getErrorResponseEntity(HttpStatus httpStatus, Exception e) {
        return ResponseEntity.status(httpStatus)
                .body(new IndividualsApiServiceErrorDto()
                        .httpStatus(httpStatus.value())
                        .message(e.getMessage()));
    }
}
