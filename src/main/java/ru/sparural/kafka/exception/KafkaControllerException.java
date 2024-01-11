package ru.sparural.kafka.exception;

import lombok.Getter;

import java.text.MessageFormat;

@Getter
public class KafkaControllerException extends Exception {
    private int statusCode = 500;

    public KafkaControllerException() {
    }

    public KafkaControllerException(int statusCode) {
        this.statusCode = statusCode;
    }

    public KafkaControllerException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public KafkaControllerException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public KafkaControllerException(String message) {
        super(message);
    }

    public KafkaControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    public KafkaControllerException(Throwable cause) {
        super(cause);
    }
}