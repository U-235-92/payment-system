package aq.project.dto;

public record ErrorDTO(Class<? extends Exception> exceptionClass, int httpStatus, String message) {
}
