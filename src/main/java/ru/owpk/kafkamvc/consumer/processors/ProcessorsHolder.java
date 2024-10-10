package ru.owpk.kafkamvc.consumer.processors;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import ru.owpk.kafkamvc.annotation.CorrelationId;
import ru.owpk.kafkamvc.annotation.MsgKey;
import ru.owpk.kafkamvc.annotation.MsgTimestamp;
import ru.owpk.kafkamvc.annotation.Payload;
import ru.owpk.kafkamvc.annotation.RequestParam;

@Slf4j
public class ProcessorsHolder {
    private static Map<Class<? extends Annotation>, AnnotationProcessor> processors = new HashMap<>();

    static {
        processors.put(MsgKey.class, new MsgKeyProcessor());
        processors.put(CorrelationId.class, new CorrelationIdProcessor());
        processors.put(Payload.class, new PayloadProcessor());
        processors.put(RequestParam.class, new RequestParamProcessor());
        processors.put(MsgTimestamp.class, new MsgTimestampProcessor());
    }

    /**
     * method can return nullable values
     * @param parameterAnnotation - sparural kafka consumer mapped method argument annotation class
     */
    public static AnnotationProcessor getProcessor(Class<? extends Annotation> parameterAnnotation) {
        if (!processors.containsKey(parameterAnnotation))
            log.warn("Detected non-processable consumer annotation: " + parameterAnnotation.getName());
        return processors.get(parameterAnnotation);
    }
}
