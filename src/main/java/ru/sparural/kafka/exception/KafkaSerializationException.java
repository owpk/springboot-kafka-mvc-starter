package ru.sparural.kafka.exception;

import java.text.MessageFormat;

public class KafkaSerializationException extends RuntimeException {

    public KafkaSerializationException() {
    }

    public KafkaSerializationException(String message) {
        super(message);
    }

    public KafkaSerializationException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public KafkaSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public KafkaSerializationException(Throwable cause) {
        super(cause);
    }

}
