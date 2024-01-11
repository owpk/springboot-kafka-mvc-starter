package ru.sparural.kafka.utils;

import ru.sparural.kafka.model.KafkaResponseMessage;

/**
 * @author Vorobyev Vyacheslav
 */
public interface KafkaResponseHandler {
    <R> R handleResponse(KafkaResponseMessage response);
}