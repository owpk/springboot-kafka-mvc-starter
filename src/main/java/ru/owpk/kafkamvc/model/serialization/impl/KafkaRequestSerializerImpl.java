package ru.owpk.kafkamvc.model.serialization.impl;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.header.Headers;

import lombok.extern.slf4j.Slf4j;
import ru.owpk.kafkamvc.KafkaMvcBaseConfig;
import ru.owpk.kafkamvc.exception.KafkaSerializationException;
import ru.owpk.kafkamvc.model.KafkaRequestMessage;
import ru.owpk.kafkamvc.model.serialization.HeaderEnum;
import ru.owpk.kafkamvc.model.serialization.KafkaRequestSerializer;
import ru.owpk.kafkamvc.model.serialization.SerializerUtils;

@Slf4j
public class KafkaRequestSerializerImpl implements KafkaRequestSerializer {

    private final SerializerUtils serializerUtils;

    public KafkaRequestSerializerImpl(String serialzerType) {
        this.serializerUtils = new SerializerUtils(serialzerType);
    }

    @Override
    public byte[] serialize(String topic, KafkaRequestMessage data) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    @Override
    public byte[] serialize(String topic, Headers headers, KafkaRequestMessage request) {
        try {
            headers.add(HeaderEnum.CORRELATION_ID.getHeader(), request.getCorrelationId());
            headers.add(HeaderEnum.REPLY_TOPIC.getHeader(), request.getReplyTopic().getBytes());
            headers.add(HeaderEnum.ACTION.getHeader(), request.getAction().getBytes());
            headers.add(HeaderEnum.REQUESTER.getHeader(), KafkaMvcBaseConfig.KAFKA_CLIENT_IDENTIFIER.getBytes());
            headers.add(HeaderEnum.TRACE_ID.getHeader(), request.getTraceId());
            var paramsBody = new String(serializerUtils.serializeMap(request.getParams()), StandardCharsets.UTF_8)
                    .getBytes();
            headers.add(HeaderEnum.PARAMS.getHeader(), paramsBody);
            return request.getPayload() != null ? request.getPayload() : new byte[0];
        } catch (Exception ex) {
            log.error("Error serialize KafkaResponseMessage", ex);
            throw new KafkaSerializationException(ex);
        }
    }
}
