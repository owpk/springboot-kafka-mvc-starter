package ru.owpk.kafkamvc.model.serialization.impl;

import org.apache.kafka.common.header.Headers;

import lombok.extern.slf4j.Slf4j;
import ru.owpk.kafkamvc.exception.KafkaSerializationException;
import ru.owpk.kafkamvc.model.KafkaRequestMessage;
import ru.owpk.kafkamvc.model.serialization.HeaderEnum;
import ru.owpk.kafkamvc.model.serialization.KafkaRequestDeserializer;
import ru.owpk.kafkamvc.model.serialization.SerializerUtils;

@Slf4j
public class KafkaRequestDeserializerImpl implements KafkaRequestDeserializer {

    private final SerializerUtils serializerUtils;

    public KafkaRequestDeserializerImpl(String serialzerType) {
        this.serializerUtils = new SerializerUtils(serialzerType);
    }

    @Override
    public KafkaRequestMessage deserialize(String topic, Headers headers, byte[] payload) {
        byte[] correlationId = null;
        byte[] traceId = null;
        String replyTopic = null;

        try {
            correlationId = serializerUtils.getRawRequiredHeader(HeaderEnum.CORRELATION_ID.getHeader(), headers);
            replyTopic = serializerUtils.getRequiredHeader(HeaderEnum.REPLY_TOPIC.getHeader(), headers);

            var tracIdHeaderVal = headers.lastHeader(HeaderEnum.TRACE_ID.getHeader());
            if (tracIdHeaderVal != null && !new String(tracIdHeaderVal.value()).isBlank())
                traceId = serializerUtils.getRawRequiredHeader(HeaderEnum.TRACE_ID.getHeader(), headers);

            KafkaRequestMessage request = new KafkaRequestMessage();
            request.setCorrelationId(correlationId);
            request.setTraceId(traceId);
            request.setReplyTopic(replyTopic);
            request.setAction(serializerUtils.getRequiredHeader(HeaderEnum.ACTION.getHeader(), headers));
            request.setParams(serializerUtils.deserializeMapFromHeader(
                    serializerUtils.getRequiredHeader(HeaderEnum.PARAMS.getHeader(), headers)));
            request.setPayload(payload);
            var requester = serializerUtils.getRequiredHeader(HeaderEnum.REQUESTER.getHeader(), headers);
            request.setRequester(requester);
            return request;
        } catch (KafkaSerializationException ex0) {
            return new KafkaRequestMessage(ex0, correlationId, traceId, replyTopic);
        } catch (RuntimeException ex1) {
            return new KafkaRequestMessage(new KafkaSerializationException(ex1), correlationId, traceId, replyTopic);
        }
    }

    @Override
    public KafkaRequestMessage deserialize(String topic, byte[] data) {
        return new KafkaRequestMessage(new KafkaSerializationException("No headers available"),
                null, null, null);
    }
}
