package ru.sparural.kafka.model.serialization.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import ru.sparural.kafka.exception.KafkaSerializationException;
import ru.sparural.kafka.model.KafkaRequestMessage;
import ru.sparural.kafka.model.serialization.HeaderEnum;
import ru.sparural.kafka.model.serialization.KafkaRequestDeserializer;
import ru.sparural.kafka.model.serialization.SerializerUtils;

import java.io.IOException;

@Slf4j
public class KafkaRequestDeserializerImpl implements KafkaRequestDeserializer {

    private final SerializerUtils serializerUtils = new SerializerUtils();

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
        } catch (IOException | RuntimeException ex1) {
            return new KafkaRequestMessage(new KafkaSerializationException(ex1), correlationId, traceId, replyTopic);
        }
    }

    @Override
    public KafkaRequestMessage deserialize(String topic, byte[] data) {
        return new KafkaRequestMessage(new KafkaSerializationException("No headers available"),
                null, null, null);
    }
}
