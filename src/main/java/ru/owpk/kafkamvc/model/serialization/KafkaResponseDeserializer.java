package ru.owpk.kafkamvc.model.serialization;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import ru.owpk.kafkamvc.model.KafkaResponseMessage;

public interface KafkaResponseDeserializer extends Deserializer<KafkaResponseMessage> {
    KafkaResponseMessage deserialize(String topic, Headers headers, byte[] payload);

    KafkaResponseMessage deserialize(String topic, byte[] data);

}
