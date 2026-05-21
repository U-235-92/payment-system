package aq.project.exceptions;

import lombok.Getter;

public class WalletException extends Exception {

    @Getter
    private final int httpStatusCode;

    public WalletException(String message, int httpStatusCode) {
        String msg = String.format("%s, http status code: %d", message, httpStatusCode);
        super(msg);
        this.httpStatusCode = httpStatusCode;
    }
}
