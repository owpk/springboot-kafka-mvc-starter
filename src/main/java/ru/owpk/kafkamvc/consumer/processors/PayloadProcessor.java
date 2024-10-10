package ru.owpk.kafkamvc.consumer.processors;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import ru.owpk.kafkamvc.exception.KafkaControllerException;
import ru.owpk.kafkamvc.model.KafkaRequestMessage;
import ru.owpk.kafkamvc.model.serialization.SerializerUtils;

public class PayloadProcessor implements AnnotationProcessor {
    private final SerializerUtils serializerUtils = new SerializerUtils();

    @Override
    public Object evaluate(Parameter parameter, ConsumerRecord<String, KafkaRequestMessage> message) throws KafkaControllerException {
        KafkaRequestMessage request = message.value();
        Type paramType = parameter.getParameterizedType();
        try {
            return serializerUtils.getPayload(paramType, request.getPayload());
        } catch (IOException | ClassNotFoundException e) {
            throw new KafkaControllerException(e);
        }
    }
}
