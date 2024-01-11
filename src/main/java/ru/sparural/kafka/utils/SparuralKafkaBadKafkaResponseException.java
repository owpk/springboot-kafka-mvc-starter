package ru.sparural.kafka.utils;

import lombok.Getter;
import ru.sparural.kafka.model.KafkaResponseMessage;

/**
 * @author Vorobyev Vyacheslav
 */
@Getter
public class SparuralKafkaBadKafkaResponseException extends RuntimeException {

    private final KafkaResponseMessage kafkaResponseMessage;

    public SparuralKafkaBadKafkaResponseException(KafkaResponseMessage kafkaResponseMessage) {
        this.kafkaResponseMessage = kafkaResponseMessage;
    }
}
