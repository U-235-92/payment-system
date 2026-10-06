package aq.project.exceptions;

public class FallbackOperationException extends RuntimeException {

    public FallbackOperationException(String message) {
        super(message);
    }

    public FallbackOperationException(String message, Exception cause) {
        super(message, cause);
    }
}
