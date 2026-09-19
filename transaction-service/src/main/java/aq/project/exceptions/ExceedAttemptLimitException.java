package aq.project.exceptions;

public class ExceedAttemptLimitException extends RuntimeException {
    public ExceedAttemptLimitException(String message) {
        super(message);
    }
}
