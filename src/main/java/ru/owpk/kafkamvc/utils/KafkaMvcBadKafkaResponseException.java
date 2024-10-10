package ru.owpk.kafkamvc.utils;

import lombok.Getter;
import ru.owpk.kafkamvc.model.KafkaResponseMessage;

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
