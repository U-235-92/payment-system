package aq.project.exceptions;

import lombok.Getter;

public class ClientHttpException extends RuntimeException {

    @Getter
    private final int statusCode;

    public ClientHttpException(int statusCode, String message) {
        this.statusCode = statusCode;
        super(message);
    }
}
