package ru.owpk.kafkamvc.consumer.processors;

import java.lang.reflect.Parameter;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import ru.owpk.kafkamvc.exception.KafkaControllerException;
import ru.owpk.kafkamvc.model.KafkaRequestMessage;

@FunctionalInterface
public interface AnnotationProcessor {
    Object evaluate(Parameter parameter, ConsumerRecord<String, KafkaRequestMessage> message) throws KafkaControllerException;
}
