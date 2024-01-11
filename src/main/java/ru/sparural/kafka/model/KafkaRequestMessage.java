package ru.sparural.kafka.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sparural.kafka.exception.KafkaSerializationException;

import java.util.Map;

@Data
@NoArgsConstructor
public class KafkaRequestMessage {

    private String action;
    private byte[] payload;
    private byte[] correlationId;
    private byte[] traceId;
    private String replyTopic;
    private Map<String, Object> params;
    private String requester;
    private KafkaSerializationException exception;

    public KafkaRequestMessage(KafkaSerializationException exception, byte[] correlationId, byte[] traceId, String replyTopic) {
        this.replyTopic = replyTopic;
        this.correlationId = correlationId;
        this.exception = exception;
    }
}
