package aq.project.exceptions;

public class ProhibitedOperationException extends RuntimeException {
    public ProhibitedOperationException(String message) {
        super(message);
    }
}
