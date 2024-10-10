package ru.owpk.kafkamvc.model.serialization;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import ru.owpk.kafkamvc.model.KafkaResponseMessage;

public interface KafkaResponseSerializer extends Serializer<KafkaResponseMessage> {

    byte[] serialize(String topic, KafkaResponseMessage data);

    byte[] serialize(String topic, Headers headers, KafkaResponseMessage response);

}
