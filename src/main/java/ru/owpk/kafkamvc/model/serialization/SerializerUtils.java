package ru.owpk.kafkamvc.model.serialization;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.owpk.kafkamvc.consumer.KafkaResponseStatus;
import ru.owpk.kafkamvc.exception.KafkaSerializationException;
import ru.owpk.kafkamvc.model.serialization.impl.BinaryPayloadSerializer;
import ru.owpk.kafkamvc.model.serialization.impl.JsonPayloadSerializer;

public class SerializerUtils {
    private final PayloadSerializer payloadSerializer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SerializerUtils(PayloadSerializer payloadSerializer) {
        this.payloadSerializer = payloadSerializer;
    }

    public SerializerUtils(String type) {
        this(switch (type) {
            case "json" ->
                new JsonPayloadSerializer();
            case "binary" ->
                new BinaryPayloadSerializer();
            default ->
                new JsonPayloadSerializer();
        });
    }

    public Object getPayload(Type payloadType, byte[] payload) throws KafkaSerializationException {
        String payloadClassName = payloadType.getTypeName();
        return getPayload(payloadClassName, payload);
    }

    public Object getPayload(Headers headers, byte[] payload) throws KafkaSerializationException {
        String payloadClassName = getRequiredHeader(HeaderEnum.PAYLOAD_TYPE.getHeader(), headers);
        return getPayload(payloadClassName, payload);
    }

    public Object getPayload(String payloadType, byte[] payload) throws KafkaSerializationException {
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

    public byte[] setPayload(Object payload, Type payloadType) throws KafkaSerializationException {
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
        if (actionHeader == null)
            throw new KafkaSerializationException("No {} header available", header);
        byte[] value = headers.lastHeader(header).value();
        if (value.length == 0)
            throw new KafkaSerializationException("No {} header available", header);
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

    public Object getObjectPayload(byte[] payload, String payloadClassName) throws KafkaSerializationException {
        try {
            return payloadSerializer.deserializeClass(payload, payloadClassName);
        } catch (Exception e) {
            throw new KafkaSerializationException(e);
        }
    }

    public byte[] setObjectPayload(Object payload) throws KafkaSerializationException {
        try {
            return payloadSerializer.serialize(payload);
        } catch (Exception e) {
            throw new KafkaSerializationException(e);
        }
    }

    public byte[] serializeMap(Map<String, Object> map) throws KafkaSerializationException {
        if (map == null)
            return new byte[0];
        try {
            return serializeFromMap(map);
        } catch (Exception e) {
            throw new KafkaSerializationException(e);
        }
    }

    public Map<String, Object> deserializeMapFromHeader(String headerValue) throws KafkaSerializationException {
        if (headerValue != null)
            try {
                return deserializeToMap(headerValue.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new KafkaSerializationException(e);
            }
        else
            return Collections.emptyMap();
    }

    public KafkaResponseStatus deserializeStatus(String requiredHeader) throws KafkaSerializationException {
        if (requiredHeader != null)
            try {
                return payloadSerializer.deserializeClass(requiredHeader.getBytes(StandardCharsets.UTF_8),
                        KafkaResponseStatus.class);
            } catch (Exception e) {
                throw new KafkaSerializationException(e);
            }
        return KafkaResponseStatus.STATUS_CODE.status(0);
    }

    public byte[] serializeFromMap(Map<String, Object> payload) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(payload);
    }

    public Map<String, Object> deserializeToMap(byte[] payload) throws StreamReadException, DatabindException, IOException {
        return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>(){});
    }
}