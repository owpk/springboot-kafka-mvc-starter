package ru.sparural.kafka.utils;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sparural.kafka.model.KafkaResponseMessage;
import ru.sparural.kafka.producer.KafkaMvcProducer;
import ru.sparural.kafka.producer.KafkaRequestInfo;

/**
 * @author Vorobyev Vyacheslav
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaMvcRequestCreator {

    private final KafkaMvcProducer kafkaMvcProducer;

    public KafkaResponseMessage send(String topic, String action) {
        return kafkaMvcProducer.send(topic, action);
    }

    public KafkaResponseMessage send(String topic, String action,
            Object body) {
        return kafkaMvcProducer.send(topic, action, body);
    }

    public KafkaResponseMessage send(Map<String, Object> params, String topic,
            String action, Object body) {
        return kafkaMvcProducer.send(params, topic, action, body);
    }

    public KafkaRequestInfo sendAsync(String topic, String action) {
        return kafkaMvcProducer.sendAsync(topic, action);
    }

    public KafkaRequestInfo sendAsync(String topic, String action, Object body) {
        return kafkaMvcProducer.sendAsync(topic, action, body);
    }

    public KafkaRequestInfo sendAsync(Map<String, Object> params, String topic, String action, Object body) {
        return kafkaMvcProducer.sendAsync(params, topic, action, body);
    }

    public <R> R sendForEntity(String topic, String action)
            throws KafkaMvcBadKafkaResponseException {
        var response = send(topic, action);
        return defaultConvertResponse(response);
    }

    public <R> R sendForEntity(String topic, String action, Object entity)
            throws KafkaMvcBadKafkaResponseException {
        var response = send(topic, action, entity);
        return defaultConvertResponse(response);
    }

    public <R> R sendForEntity(Map<String, Object> params, String topic,
            String action, Object entity)
            throws KafkaMvcBadKafkaResponseException {
        var response = send(params, topic, action, entity);
        return defaultConvertResponse(response);
    }

    private <R> R defaultConvertResponse(KafkaResponseMessage response)
            throws KafkaMvcBadKafkaResponseException {
        return DefaultKafkaResponseHandler.getInstance().handleResponse(response);
    }

    public <R> R sendForEntity(String topic, String action,
            KafkaResponseHandler kafkaResponseHandler)
            throws KafkaMvcBadKafkaResponseException {
        var response = send(topic, action);
        return defaultResponseHandlerResponse(kafkaResponseHandler, response);
    }

    public <R> R sendForEntity(String topic, String action, Object entity,
            KafkaResponseHandler kafkaResponseHandler)
            throws KafkaMvcBadKafkaResponseException {
        var response = send(topic, action, entity);
        return defaultResponseHandlerResponse(kafkaResponseHandler, response);
    }

    public <R> R sendForEntity(Map<String, Object> params, String topic,
            String action, Object entity,
            KafkaResponseHandler kafkaResponseHandler)
            throws KafkaMvcBadKafkaResponseException {
        var response = send(params, topic, action, entity);
        return defaultResponseHandlerResponse(kafkaResponseHandler, response);
    }

    private <R> R defaultResponseHandlerResponse(KafkaResponseHandler kafkaResponseHandler,
            KafkaResponseMessage response) {
        if (kafkaResponseHandler == null)
            return DefaultKafkaResponseHandler.getInstance().handleResponse(response);
        return kafkaResponseHandler.handleResponse(response);
    }

    public KafkaMvcRequestBuilder createRequestBuilder() {
        return new KafkaMvcRequestBuilder(this);
    }

}