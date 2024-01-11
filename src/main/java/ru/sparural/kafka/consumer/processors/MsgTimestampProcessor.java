package ru.sparural.kafka.consumer.processors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.sparural.kafka.annotation.CorrelationId;
import ru.sparural.kafka.exception.KafkaControllerException;
import ru.sparural.kafka.model.KafkaRequestMessage;

import java.lang.reflect.Parameter;

public class MsgTimestampProcessor implements AnnotationProcessor {

    @Override
    public Object evaluate(Parameter parameter, ConsumerRecord<String, KafkaRequestMessage> message) throws KafkaControllerException {
        if (!(parameter.getType().equals(Long.class) || parameter.getType().equals(long.class)))
            throw new KafkaControllerException("MsgTimestamp annotated parameter must be Long");
        return message.timestamp();
    }
}
