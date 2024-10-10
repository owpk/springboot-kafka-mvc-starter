package ru.sparural.kafka.utils;

import lombok.Getter;
import ru.sparural.kafka.model.KafkaResponseMessage;

/**
 * @author Vorobyev Vyacheslav
 */
@Getter
public class KafkaMvcBadKafkaResponseException extends RuntimeException {

    private final KafkaResponseMessage kafkaResponseMessage;

    public KafkaMvcBadKafkaResponseException(KafkaResponseMessage kafkaResponseMessage) {
        this.kafkaResponseMessage = kafkaResponseMessage;
    }
}
