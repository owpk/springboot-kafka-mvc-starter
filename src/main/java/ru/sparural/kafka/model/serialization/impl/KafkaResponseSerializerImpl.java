/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sparural.kafka.model.serialization.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import ru.sparural.kafka.KafkaSparuralBaseConfig;
import ru.sparural.kafka.consumer.KafkaResponseStatus;
import ru.sparural.kafka.model.KafkaResponseMessage;
import ru.sparural.kafka.model.serialization.HeaderEnum;
import ru.sparural.kafka.model.serialization.KafkaPayloadType;
import ru.sparural.kafka.model.serialization.KafkaResponseSerializer;
import ru.sparural.kafka.model.serialization.SerializerUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class KafkaResponseSerializerImpl implements KafkaResponseSerializer {

    private final SerializerUtils serializerUtils = new SerializerUtils();

    @Override
    public byte[] serialize(String topic, KafkaResponseMessage data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] serialize(String topic, Headers headers, KafkaResponseMessage response) {
        try {
            String payloadType;
            byte[] result;
            if (response.getPayload() == null) {
                result = new byte[0];
                payloadType = KafkaPayloadType.VOID.getPayloadType();
            } else {
                result = serializerUtils.setPayload(response.getPayload(), response.getPayloadType());
                payloadType = response.getPayloadType().getTypeName();
            }
            headers.add(HeaderEnum.CORRELATION_ID.getHeader(), response.getCorrelationId());
            headers.add(HeaderEnum.PAYLOAD_TYPE.getHeader(), payloadType.getBytes());
            headers.add(HeaderEnum.RESPONDENT.getHeader(), KafkaSparuralBaseConfig.KAFKA_CLIENT_IDENTIFIER.getBytes());
            int statusCode = response.getStatus().getCode();
            headers.add(HeaderEnum.STATUS.getHeader(), serializerUtils.setIntegerPayload(statusCode));
            return result;
        } catch (JsonProcessingException ex) {
            log.error("Error serialize KafkaResponseMessage", ex);
            headers.add(HeaderEnum.CORRELATION_ID.getHeader(), response.getCorrelationId());
            headers.add(HeaderEnum.PAYLOAD_TYPE.getHeader(), KafkaPayloadType.STRING.getPayloadType().getBytes());
            headers.add(HeaderEnum.STATUS.getHeader(), KafkaResponseStatus.SERVER_ERROR.name().getBytes());
            return ex.getMessage().getBytes();
        }
    }

}
