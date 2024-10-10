package ru.owpk.kafkamvc.consumer.processors;

import java.lang.reflect.Parameter;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import ru.owpk.kafkamvc.annotation.RequestParam;
import ru.owpk.kafkamvc.exception.KafkaControllerException;
import ru.owpk.kafkamvc.model.KafkaRequestMessage;

public class RequestParamProcessor implements AnnotationProcessor {

    @Override
    public Object evaluate(Parameter parameter, ConsumerRecord<String, KafkaRequestMessage> message) throws KafkaControllerException {
        return extractParameter(message.value().getParams(), parameter);
    }

    public Object extractParameter(Map<String, Object> params, Parameter parameter) {
        var annotation = parameter.getAnnotation(RequestParam.class);
        String key = annotation.value();

        if (key.isBlank())
            key = parameter.getName();

        var parameterType = parameter.getType();
        var value = params.getOrDefault(key, null);
        if (value == null) {
            return null;
        }
        if (Number.class.isAssignableFrom(parameterType)) {
            Number number = (Number) value;
            if (parameterType.equals(Long.class)
                    && number.getClass().equals(Integer.class)) {
                return Long.valueOf((Integer) number);
            } else if (parameterType.equals(Integer.class)
                    && value.getClass().equals(Long.class)) {
                throw new IllegalArgumentException("Can't cast " + parameterType + " to " + number.getClass());
            } else {
                return value;
            }
        }
        return value;
    }
}
