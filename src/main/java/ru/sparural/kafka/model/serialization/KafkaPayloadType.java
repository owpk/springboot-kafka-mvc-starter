package ru.sparural.kafka.model.serialization;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum KafkaPayloadType {
    STRING(String.class.getName()),
    LONG(Long.class.getName()),
    INTEGER(Integer.class.getName()),
    DEFAULT(Object.class.getName()),
    VOID(Void.class.getName());

    private static final Map<String, KafkaPayloadType> payloadMap = Stream.of(KafkaPayloadType.values())
            .collect(Collectors.toMap(KafkaPayloadType::getPayloadType, v -> v));
    private final String payloadType;

    private KafkaPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    public static KafkaPayloadType of(String payloadType) {
        return payloadMap.getOrDefault(payloadType, DEFAULT);
    }

    public String getPayloadType() {
        return payloadType;
    }
}
