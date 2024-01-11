package ru.sparural.kafka.consumer.processors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.sparural.kafka.annotation.CorrelationId;
import ru.sparural.kafka.exception.KafkaControllerException;
import ru.sparural.kafka.model.KafkaRequestMessage;

import java.lang.reflect.Parameter;

public class CorrelationIdProcessor implements AnnotationProcessor {

    @Override
    public Object evaluate(Parameter parameter, ConsumerRecord<String, KafkaRequestMessage> message) throws KafkaControllerException {
        if (!parameter.getType().equals(String.class))
            throw new KafkaControllerException("MsgKey annotated parameter must be String");
        return new String(message.value().getCorrelationId());
    }
}
