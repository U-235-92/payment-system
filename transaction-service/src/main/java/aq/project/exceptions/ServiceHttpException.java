package aq.project.exceptions;

import lombok.Getter;

public class ServiceHttpException extends RuntimeException {

    @Getter
    private final int statusCode;

    public ServiceHttpException(int statusCode, String message) {
        this.statusCode = statusCode;
        super(message);
    }
}
