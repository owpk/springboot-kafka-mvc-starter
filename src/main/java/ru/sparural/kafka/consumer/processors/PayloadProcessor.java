package ru.sparural.kafka.consumer.processors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.sparural.kafka.annotation.Payload;
import ru.sparural.kafka.exception.KafkaControllerException;
import ru.sparural.kafka.model.KafkaRequestMessage;
import ru.sparural.kafka.model.serialization.SerializerUtils;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

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
