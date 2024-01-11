package ru.sparural.kafka.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sparural.kafka.model.KafkaResponseMessage;
import ru.sparural.kafka.producer.KafkaRequestInfo;
import ru.sparural.kafka.producer.KafkaSparuralProducer;

import java.util.Map;

/**
 * @author Vorobyev Vyacheslav
 */
@Slf4j
@RequiredArgsConstructor
public class SparuralKafkaRequestCreator {

    private final KafkaSparuralProducer kafkaSparuralProducer;

    public KafkaResponseMessage send(String topic, String action) {
        return kafkaSparuralProducer.send(topic, action);
    }

    public KafkaResponseMessage send(String topic, String action,
                                     Object body) {
        return kafkaSparuralProducer.send(topic, action, body);
    }

    public KafkaResponseMessage send(Map<String, Object> params, String topic,
                                     String action, Object
            body) {
        return kafkaSparuralProducer.send(params, topic, action, body);
    }

    public KafkaRequestInfo sendAsync(String topic, String action) {
        return kafkaSparuralProducer.sendAsync(topic, action);
    }

    public KafkaRequestInfo sendAsync(String topic, String action, Object body) {
        return kafkaSparuralProducer.sendAsync(topic, action, body);
    }

    public KafkaRequestInfo sendAsync(Map<String, Object> params, String topic, String action, Object
            body) {
        return kafkaSparuralProducer.sendAsync(params, topic, action, body);
    }

    public <R> R sendForEntity(String topic, String action)
            throws SparuralKafkaBadKafkaResponseException {
        var response = send(topic, action);
        return defaultConvertResponse(response);
    }

    public <R> R sendForEntity(String topic, String action, Object entity)
            throws SparuralKafkaBadKafkaResponseException {
        var response = send(topic, action, entity);
        return defaultConvertResponse(response);
    }

    public <R> R sendForEntity(Map<String, Object> params, String topic,
                               String action, Object entity)
            throws SparuralKafkaBadKafkaResponseException {
        var response = send(params, topic, action, entity);
        return defaultConvertResponse(response);
    }

    private <R> R defaultConvertResponse(KafkaResponseMessage response)
            throws SparuralKafkaBadKafkaResponseException {
        return DefaultKafkaResponseHandler.getInstance().handleResponse(response);
    }

    public <R> R sendForEntity(String topic, String action,
                               KafkaResponseHandler kafkaResponseHandler)
            throws SparuralKafkaBadKafkaResponseException {
        var response = send(topic, action);
        return defaultResponseHandlerResponse(kafkaResponseHandler, response);
    }

    public <R> R sendForEntity(String topic, String action, Object entity,
                               KafkaResponseHandler kafkaResponseHandler)
            throws SparuralKafkaBadKafkaResponseException {
        var response = send(topic, action, entity);
        return defaultResponseHandlerResponse(kafkaResponseHandler, response);
    }

    public <R> R sendForEntity(Map<String, Object> params, String topic,
                               String action, Object entity,
                               KafkaResponseHandler kafkaResponseHandler)
            throws SparuralKafkaBadKafkaResponseException {
        var response = send(params, topic, action, entity);
        return defaultResponseHandlerResponse(kafkaResponseHandler, response);
    }

    private <R> R defaultResponseHandlerResponse(KafkaResponseHandler kafkaResponseHandler,
                                                 KafkaResponseMessage response) {
        if (kafkaResponseHandler == null)
            return DefaultKafkaResponseHandler.getInstance().handleResponse(response);
        return kafkaResponseHandler.handleResponse(response);
    }

    public SparuralKafkaRequestBuilder createRequestBuilder() {
        return new SparuralKafkaRequestBuilder(this);
    }

}