package ru.sparural.kafka.model.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.util.StringUtils;
import ru.sparural.kafka.consumer.KafkaResponseStatus;
import ru.sparural.kafka.exception.KafkaSerializationException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

public class SerializerUtils {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object getPayload(Type payloadType, byte[] payload) throws IOException, ClassNotFoundException {
        String payloadClassName = payloadType.getTypeName();
        return getPayload(payloadClassName, payload);
    }

    public Object getPayload(Headers headers, byte[] payload) throws IOException, ClassNotFoundException {
        String payloadClassName = getRequiredHeader(HeaderEnum.PAYLOAD_TYPE.getHeader(), headers);
        return getPayload(payloadClassName, payload);
    }

    public Object getPayload(String payloadType, byte[] payload) throws IOException, ClassNotFoundException {
        KafkaPayloadType kafkaPayloadType = KafkaPayloadType.of(payloadType);
        switch (kafkaPayloadType) {
            case STRING:
                return new String(payload);
            case LONG:
                return getLongPayload(payload);
            case INTEGER:
                return getIntegerPayload(payload);
            case DEFAULT:
                return getObjectPayload(payload, payloadType);
            default:
                throw new KafkaSerializationException("Unsupported payload type {}", payloadType);
        }
    }

    public byte[] setPayload(Object payload, Type payloadType) throws JsonProcessingException {
        if (payload == null) {
            return new byte[0];
        }

        KafkaPayloadType kafkaPayloadType = payloadType != null ? KafkaPayloadType.of(payloadType.getTypeName())
                : KafkaPayloadType.of(payload.getClass().getName());

        switch (kafkaPayloadType) {
            case STRING:
                return ((String) payload).getBytes();
            case LONG:
                return setLongPayload((Long) payload);
            case INTEGER:
                return setIntegerPayload((Integer) payload);
            case DEFAULT:
            case VOID:
                return setObjectPayload(payload);
            default:
                throw new KafkaSerializationException("Unsupported payload type {}", payloadType.getTypeName());
        }
    }

    public String getRequiredHeader(String header, Headers headers) {
        Header actionHeader = headers.lastHeader(header);
        if (actionHeader == null)
            throw new KafkaSerializationException("No {} header available", header);
        String value = new String(actionHeader.value());
        if (!StringUtils.hasText(value))
            throw new KafkaSerializationException("No {} header available", header);
        return value;
    }

    public byte[] getRequiredHeaderInBytes(String header, Headers headers) {
        Header actionHeader = headers.lastHeader(header);
        if (actionHeader == null)
            throw new KafkaSerializationException("No {} header available", header);
        if (actionHeader.value().length == 0)
            throw new KafkaSerializationException("No {} header available", header);
        return actionHeader.value();
    }

    public byte[] getRawRequiredHeader(String header, Headers headers) {
        Header actionHeader = headers.lastHeader(header);
        if (actionHeader == null) {
            throw new KafkaSerializationException("No {} header available", header);
        }
        byte[] value = headers.lastHeader(header).value();

        if (value.length == 0) {
            throw new KafkaSerializationException("No {} header available", header);
        }

        return value;
    }

    public Integer getIntegerPayload(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(payload);
        buffer.flip();
        return buffer.getInt();
    }

    public byte[] setIntegerPayload(Integer payload) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(0, payload);
        return buffer.array();
    }

    public Long getLongPayload(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(payload);
        buffer.flip();
        return buffer.getLong();
    }

    public byte[] setLongPayload(Long payload) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, payload);
        return buffer.array();
    }

    public Object getObjectPayload(byte[] payload, String payloadClassName) throws IOException, ClassNotFoundException {
        JavaType javaType = objectMapper.getTypeFactory().constructFromCanonical(payloadClassName);
        return objectMapper.readValue(payload, javaType);
    }

    public byte[] setObjectPayload(Object payload) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(payload);
    }

    public byte[] serializeMap(Map<String, Object> map) throws JsonProcessingException {
        if (map == null)
            return new byte[0];
        return objectMapper.writeValueAsBytes(map);
    }

    public Map<String, Object> deserializeMapFromHeader(String headerValue) throws IOException {
        if (headerValue != null)
            return objectMapper.readValue(headerValue, new TypeReference<>() {
            });
        else return Collections.emptyMap();
    }

    public KafkaResponseStatus deserializeStatus(String requiredHeader) throws JsonProcessingException {
        if (requiredHeader != null)
            return objectMapper.readValue(requiredHeader, KafkaResponseStatus.class);
        return KafkaResponseStatus.STATUS_CODE.status(0);
    }
}