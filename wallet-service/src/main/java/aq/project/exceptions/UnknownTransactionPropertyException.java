package aq.project.exceptions;

public class UnknownTransactionPropertyException extends RuntimeException {
    public UnknownTransactionPropertyException(String message) {
        super(message);
    }
}
