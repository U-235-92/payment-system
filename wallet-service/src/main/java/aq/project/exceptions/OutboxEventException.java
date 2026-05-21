package aq.project.exceptions;

public class OutboxEventException extends Exception {
    public OutboxEventException(String message) {
        super(message);
    }
}
