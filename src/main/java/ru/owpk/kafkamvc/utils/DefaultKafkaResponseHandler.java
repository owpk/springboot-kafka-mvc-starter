package ru.owpk.kafkamvc.utils;

import lombok.extern.slf4j.Slf4j;
import ru.owpk.kafkamvc.consumer.KafkaResponseStatus;
import ru.owpk.kafkamvc.model.KafkaResponseMessage;

/**
 * @author Vorobyev Vyacheslav
 */
@Slf4j
public class DefaultKafkaResponseHandler implements KafkaResponseHandler {

    private static DefaultKafkaResponseHandler instance;

    public static KafkaResponseHandler getInstance() {
        if (instance == null)
            instance = new DefaultKafkaResponseHandler();
        return instance;
    }

    public <R> R handleResponse(KafkaResponseMessage response) {
        if (successCondition(response)) {
            try {
                return response.getPayload() == null ? null : castToObject(response.getPayload());
            } catch (ClassCastException c) {
                log.error(String.format("Unknown service response type. Status %s. Exception message : %s",
                        response.getStatus(), c.getLocalizedMessage()), c);
                throw new KafkaMvcBadKafkaResponseException(response);
            }
        } else
            throw new KafkaMvcBadKafkaResponseException(response);
    }

    @SuppressWarnings("unchecked")
    private <R> R castToObject(Object response) {
        return (R) response;
    }

    private boolean successCondition(KafkaResponseMessage kafkaResponseMessage) {
        return kafkaResponseMessage.getStatus().getCode() >= KafkaResponseStatus.SUCCESS.getCode() &&
                kafkaResponseMessage.getStatus().getCode() < KafkaResponseStatus.MULTIPLE_CHOOSE.getCode();
    }
}