/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.owpk.kafkamvc.model.serialization.impl;

import org.apache.kafka.common.header.Headers;

import lombok.extern.slf4j.Slf4j;
import ru.owpk.kafkamvc.KafkaMvcBaseConfig;
import ru.owpk.kafkamvc.consumer.KafkaResponseStatus;
import ru.owpk.kafkamvc.model.KafkaResponseMessage;
import ru.owpk.kafkamvc.model.serialization.HeaderEnum;
import ru.owpk.kafkamvc.model.serialization.KafkaPayloadType;
import ru.owpk.kafkamvc.model.serialization.KafkaResponseSerializer;
import ru.owpk.kafkamvc.model.serialization.SerializerUtils;

@Slf4j
public class KafkaResponseSerializerImpl implements KafkaResponseSerializer {

    private final SerializerUtils serializerUtils;

    public KafkaResponseSerializerImpl(String serialzerType) {
        this.serializerUtils = new SerializerUtils(serialzerType);
    }

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
            headers.add(HeaderEnum.RESPONDENT.getHeader(), KafkaMvcBaseConfig.KAFKA_CLIENT_IDENTIFIER.getBytes());
            int statusCode = response.getStatus().getCode();
            headers.add(HeaderEnum.STATUS.getHeader(), serializerUtils.setIntegerPayload(statusCode));
            return result;
        } catch (Exception ex) {
            log.error("Error serialize KafkaResponseMessage", ex);
            headers.add(HeaderEnum.CORRELATION_ID.getHeader(), response.getCorrelationId());
            headers.add(HeaderEnum.PAYLOAD_TYPE.getHeader(), KafkaPayloadType.STRING.getPayloadType().getBytes());
            headers.add(HeaderEnum.STATUS.getHeader(), KafkaResponseStatus.SERVER_ERROR.name().getBytes());
            return ex.getMessage().getBytes();
        }
    }

}
