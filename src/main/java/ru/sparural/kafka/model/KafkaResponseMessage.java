package ru.sparural.kafka.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import ru.sparural.kafka.consumer.KafkaResponseStatus;
import ru.sparural.kafka.exception.KafkaSerializationException;

import java.lang.reflect.Type;

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
