package ru.sparural.kafka.model.serialization.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import ru.sparural.kafka.KafkaSparuralBaseConfig;
import ru.sparural.kafka.exception.KafkaSerializationException;
import ru.sparural.kafka.model.KafkaRequestMessage;
import ru.sparural.kafka.model.serialization.HeaderEnum;
import ru.sparural.kafka.model.serialization.KafkaRequestSerializer;
import ru.sparural.kafka.model.serialization.SerializerUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
public class KafkaRequestSerializerImpl implements KafkaRequestSerializer {

    private final SerializerUtils serializerUtils = new SerializerUtils();

    @Override
    public byte[] serialize(String topic, KafkaRequestMessage data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] serialize(String topic, Headers headers, KafkaRequestMessage request) {
        try {
            headers.add(HeaderEnum.CORRELATION_ID.getHeader(), request.getCorrelationId());
            headers.add(HeaderEnum.REPLY_TOPIC.getHeader(), request.getReplyTopic().getBytes());
            headers.add(HeaderEnum.ACTION.getHeader(), request.getAction().getBytes());
            headers.add(HeaderEnum.REQUESTER.getHeader(), KafkaSparuralBaseConfig.KAFKA_CLIENT_IDENTIFIER.getBytes());
            headers.add(HeaderEnum.TRACE_ID.getHeader(), request.getTraceId());
            var paramsBody = new String(serializerUtils.serializeMap(request.getParams()), StandardCharsets.UTF_8).getBytes();
            headers.add(HeaderEnum.PARAMS.getHeader(), paramsBody);
            return request.getPayload() != null ? request.getPayload() : new byte[0];
        } catch (JsonProcessingException ex) {
            log.error("Error serialize KafkaResponseMessage", ex);
            throw new KafkaSerializationException(ex);
        }
    }
}
