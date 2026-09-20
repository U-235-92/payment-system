package aq.project.exceptions;

import lombok.Getter;

@Getter
public class NotFoundTopicException extends RuntimeException {

    private final String topic;

    public NotFoundTopicException(String topic, String message) {
        super(message);
        this.topic = topic;
    }
}
