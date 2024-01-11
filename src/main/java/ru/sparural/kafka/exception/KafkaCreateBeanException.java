package ru.sparural.kafka.exception;

public class KafkaCreateBeanException extends RuntimeException {

    public KafkaCreateBeanException() {
    }

    public KafkaCreateBeanException(String message) {
        super(message);
    }

    public KafkaCreateBeanException(String message, Throwable cause) {
        super(message, cause);
    }

    public KafkaCreateBeanException(Throwable cause) {
        super(cause);
    }
}
