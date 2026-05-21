package aq.project.exceptions;

public class UnknownOutboxEventPropertyException extends RuntimeException {
    public UnknownOutboxEventPropertyException(String message) {
        super(message);
    }
}
