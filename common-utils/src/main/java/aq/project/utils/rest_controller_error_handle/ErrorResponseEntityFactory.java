package aq.project.utils.rest_controller_error_handle;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ErrorResponseEntityFactory {

    public static <T> ResponseEntity<T> getErrorResponseEntity(
            T dto,
            HttpStatus httpStatus
    ) {
        return ResponseEntity.status(httpStatus)
                .body(dto);
    }
}
