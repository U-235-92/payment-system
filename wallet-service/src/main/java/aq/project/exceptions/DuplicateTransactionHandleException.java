package aq.project.exceptions;

public class DuplicateTransactionHandleException extends RuntimeException {
    public DuplicateTransactionHandleException(String message) {
        super(message);
    }
}
