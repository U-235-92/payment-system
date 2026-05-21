package aq.project.exceptions;

import lombok.Getter;

public class TransactionException extends Exception {

    @Getter
    private final int httpStatusCode;

    public TransactionException(String message, int httpStatusCode) {
        String msg = String.format("%s, http status code: %d", message, httpStatusCode);
        super(msg);
        this.httpStatusCode = httpStatusCode;
    }
}
