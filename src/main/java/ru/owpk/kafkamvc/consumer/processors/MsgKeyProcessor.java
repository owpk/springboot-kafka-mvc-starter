package ru.owpk.kafkamvc.consumer.processors;


import java.lang.reflect.Parameter;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import ru.owpk.kafkamvc.exception.KafkaControllerException;
import ru.owpk.kafkamvc.model.KafkaRequestMessage;

public class MsgKeyProcessor implements AnnotationProcessor {

    @Override
    public Object evaluate(Parameter parameter, ConsumerRecord<String, KafkaRequestMessage> message) throws KafkaControllerException {
        if (!parameter.getType().equals(String.class))
            throw new KafkaControllerException("MsgKey annotated parameter must be String");
        return message.key();
    }
}
