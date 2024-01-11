package ru.sparural.kafka.utils;

import lombok.Getter;
import lombok.Setter;
import ru.sparural.kafka.model.KafkaResponseMessage;
import ru.sparural.kafka.producer.KafkaRequestInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Vorobyev Vyacheslav
 */
@Getter
@Setter
public class SparuralKafkaRequestBuilder {
    private String topic;
    private String action;
    private Map<String, Object> parameters;
    private Object body;
    private Supplier<SparuralKafkaBadKafkaResponseException> exceptionSupplier;

    private SparuralKafkaRequestCreator kafkaRequestCreator;
    private Class<?> responseType;

    public SparuralKafkaRequestBuilder(SparuralKafkaRequestCreator kafkaRequestCreator) {
        this.kafkaRequestCreator = kafkaRequestCreator;
        parameters = new HashMap<>();
    }

    public SparuralKafkaRequestBuilder withRequestParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    public SparuralKafkaRequestBuilder withRequestParameters(Map<String, Object> headers) {
        this.parameters = headers;
        return this;
    }

    public SparuralKafkaRequestBuilder withRequestBody(Object body) {
        this.body = body;
        return this;
    }

    public SparuralKafkaRequestBuilder withTopicName(String topic) {
        this.topic = topic;
        return this;
    }

    public SparuralKafkaRequestBuilder withAction(String action) {
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

    public <R> R sendForEntity() throws SparuralKafkaBadKafkaResponseException {
        defaultCheck();
        return kafkaRequestCreator.sendForEntity(parameters, topic, action, body);
    }

    public <R> R sendForEntity(Class<R> cl) throws SparuralKafkaBadKafkaResponseException {
        defaultCheck();
        return kafkaRequestCreator.sendForEntity(parameters, topic, action, body, DefaultKafkaResponseHandler.getInstance());
    }

    private void defaultCheck() {
        if (topic == null || action == null)
            throw new RuntimeException("Topic name or action name must be present");
    }
}