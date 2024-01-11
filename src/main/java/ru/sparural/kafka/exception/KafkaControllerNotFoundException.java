package ru.sparural.kafka.exception;

public class KafkaControllerNotFoundException extends Exception {
    private final String topic;
    private final String action;

    public KafkaControllerNotFoundException(String topic, String action) {
        super();
        this.topic = topic;
        this.action = action;
    }

    public String getTopic() {
        return topic;
    }

    public String getAction() {
        return action;
    }
}
