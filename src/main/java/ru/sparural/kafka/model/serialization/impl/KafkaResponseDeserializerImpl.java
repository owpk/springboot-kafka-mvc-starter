package ru.sparural.kafka.model.serialization.impl;

import org.apache.kafka.common.header.Headers;
import ru.sparural.kafka.consumer.KafkaResponseStatus;
import ru.sparural.kafka.exception.KafkaSerializationException;
import ru.sparural.kafka.model.KafkaResponseMessage;
import ru.sparural.kafka.model.serialization.HeaderEnum;
import ru.sparural.kafka.model.serialization.KafkaResponseDeserializer;
import ru.sparural.kafka.model.serialization.SerializerUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class KafkaResponseDeserializerImpl implements KafkaResponseDeserializer {
    private final SerializerUtils serializerUtils = new SerializerUtils();

    @Override
    public KafkaResponseMessage deserialize(String topic, Headers headers, byte[] payload) {
        byte[] correlationId = null;
        try {
            KafkaResponseMessage response = new KafkaResponseMessage();
            correlationId = serializerUtils.getRawRequiredHeader(HeaderEnum.CORRELATION_ID.getHeader(), headers);
            int statusCode = serializerUtils.getIntegerPayload(
                    serializerUtils.getRequiredHeaderInBytes(HeaderEnum.STATUS.getHeader(), headers));
            response.setStatus(KafkaResponseStatus.valueOf(statusCode));
            if (payload.length > 0)
                response.setPayload(serializerUtils.getPayload(headers, payload));
            response.setCorrelationId(correlationId);
            var respondent = serializerUtils.getRawRequiredHeader(HeaderEnum.RESPONDENT.getHeader(), headers);
            response.setRespondent(new String(respondent, StandardCharsets.UTF_8));
            return response;
        } catch (KafkaSerializationException ex0) {
            return new KafkaResponseMessage(ex0, correlationId);
        } catch (IOException | ClassNotFoundException | RuntimeException ex1) {
            return new KafkaResponseMessage(new KafkaSerializationException(ex1), correlationId);
        }
    }

    @Override
    public KafkaResponseMessage deserialize(String topic, byte[] data) {
        KafkaResponseMessage response = new KafkaResponseMessage();
        response.setStatus(KafkaResponseStatus.INVALID_RESPONSE);
        return response;
    }

}
