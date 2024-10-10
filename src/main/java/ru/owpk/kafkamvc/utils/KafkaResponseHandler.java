package ru.owpk.kafkamvc.utils;

import ru.owpk.kafkamvc.model.KafkaResponseMessage;

/**
 * @author Vorobyev Vyacheslav
 */
public interface KafkaResponseHandler {
    <R> R handleResponse(KafkaResponseMessage response);
}