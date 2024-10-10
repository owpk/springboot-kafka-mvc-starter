package ru.owpk.kafkamvc.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;
import ru.owpk.kafkamvc.model.KafkaResponseMessage;
import ru.owpk.kafkamvc.producer.KafkaRequestInfo;

/**
 * @author Vorobyev Vyacheslav
 */
@Getter
@Setter
public class KafkaMvcRequestBuilder {
    private String topic;
    private String action;
    private Map<String, Object> parameters;
    private Object body;
    private Supplier<KafkaMvcBadKafkaResponseException> exceptionSupplier;

    private KafkaMvcRequestCreator kafkaRequestCreator;
    private Class<?> responseType;

    public KafkaMvcRequestBuilder(KafkaMvcRequestCreator kafkaRequestCreator) {
        this.kafkaRequestCreator = kafkaRequestCreator;
        parameters = new HashMap<>();
    }

    public KafkaMvcRequestBuilder withRequestParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    public KafkaMvcRequestBuilder withRequestParameters(Map<String, Object> headers) {
        this.parameters = headers;
        return this;
    }

    public KafkaMvcRequestBuilder withRequestBody(Object body) {
        this.body = body;
        return this;
    }

    public KafkaMvcRequestBuilder withTopicName(String topic) {
        this.topic = topic;
        return this;
    }

    public KafkaMvcRequestBuilder withAction(String action) {
        this.action = action;
        return this;
    }

    public KafkaResponseMessage send() {
        defaultCheck();
        return kafkaRequestCreator.send(parameters, topic, action, body);
    }

    public KafkaRequestInfo sendAsync() {
        defaultCheck();
        return kafkaRequestCreator.sendAsync(parameters, topic, action, body);
    }

    public <R> R sendForEntity(Class<R> cl, KafkaResponseHandler kafkaResponseHandler) {
        defaultCheck();
        return kafkaRequestCreator.sendForEntity(parameters, topic, action, body, kafkaResponseHandler);
    }

    public <R> R sendForEntity(KafkaResponseHandler kafkaResponseHandler) {
        defaultCheck();
        return kafkaRequestCreator.sendForEntity(parameters, topic, action, body, kafkaResponseHandler);
    }

    public <R> R sendForEntity() throws KafkaMvcBadKafkaResponseException {
        defaultCheck();
        return kafkaRequestCreator.sendForEntity(parameters, topic, action, body);
    }

    public <R> R sendForEntity(Class<R> cl) throws KafkaMvcBadKafkaResponseException {
        defaultCheck();
        return kafkaRequestCreator.sendForEntity(parameters, topic, action, body,
                DefaultKafkaResponseHandler.getInstance());
    }

    private void defaultCheck() {
        if (topic == null || action == null)
            throw new RuntimeException("Topic name or action name must be present");
    }
}