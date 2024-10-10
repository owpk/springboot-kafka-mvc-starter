package ru.sparural.kafka.producer;

import java.util.Map;

import ru.sparural.kafka.model.KafkaResponseMessage;

public interface KafkaMvcProducer {
    KafkaResponseMessage send(String topic, String action);

    KafkaResponseMessage send(String topic, String action, Object payload);

    KafkaResponseMessage send(Map<String, Object> params, String topic, String action);

    KafkaResponseMessage send(Map<String, Object> params, String topic, String action, Object payload);

    KafkaRequestInfo sendAsync(String topic, String action);

    KafkaRequestInfo sendAsync(String topic, String action, Object payload);

    KafkaRequestInfo sendAsync(Map<String, Object> params, String topic, String action);

    KafkaRequestInfo sendAsync(Map<String, Object> params, String topic, String action, Object payload);
}
