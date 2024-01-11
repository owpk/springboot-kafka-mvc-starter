package ru.sparural.kafka.model.serialization;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import ru.sparural.kafka.model.KafkaRequestMessage;

public interface KafkaRequestSerializer extends Serializer<KafkaRequestMessage> {

    byte[] serialize(String topic, KafkaRequestMessage data);

    byte[] serialize(String topic, Headers headers, KafkaRequestMessage request);
}
