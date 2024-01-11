package ru.sparural.kafka.model.serialization;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import ru.sparural.kafka.model.KafkaRequestMessage;

public interface KafkaRequestDeserializer extends Deserializer<KafkaRequestMessage> {

    KafkaRequestMessage deserialize(String topic, Headers headers, byte[] payload);

    KafkaRequestMessage deserialize(String topic, byte[] data);
}
