package aq.project.exceptions;

public class UnknownOutboxEventTypeException extends Exception {
    public UnknownOutboxEventTypeException(String message) {
        super(message);
    }
}
