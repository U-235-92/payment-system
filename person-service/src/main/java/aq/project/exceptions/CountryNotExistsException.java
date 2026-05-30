package aq.project.exceptions;

public class CountryNotExistsException extends RuntimeException {
    public CountryNotExistsException(String message) {
        super(message);
    }
}
