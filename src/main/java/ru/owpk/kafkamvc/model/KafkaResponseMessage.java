package ru.owpk.kafkamvc.model;

import java.lang.reflect.Type;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.owpk.kafkamvc.consumer.KafkaResponseStatus;
import ru.owpk.kafkamvc.exception.KafkaSerializationException;

@Data
@NoArgsConstructor
public class KafkaResponseMessage {

    private KafkaResponseStatus status;
    private Object payload;
    private Type payloadType;
    private byte[] correlationId;
    private String replyTopic;
    private String respondent;
    private KafkaSerializationException exception;

    public KafkaResponseMessage(KafkaSerializationException exception, byte[] correlationId) {
        this.status = KafkaResponseStatus.INVALID_RESPONSE;
        this.correlationId = correlationId;
        this.exception = exception;
    }
}
