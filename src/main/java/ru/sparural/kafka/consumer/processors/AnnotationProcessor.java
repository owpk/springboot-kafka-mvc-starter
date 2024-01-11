package ru.sparural.kafka.consumer.processors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.sparural.kafka.exception.KafkaControllerException;
import ru.sparural.kafka.model.KafkaRequestMessage;

import java.lang.reflect.Parameter;

@FunctionalInterface
public interface AnnotationProcessor {
    Object evaluate(Parameter parameter, ConsumerRecord<String, KafkaRequestMessage> message) throws KafkaControllerException;
}
