package ru.sparural.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.lang.Nullable;
import ru.sparural.kafka.KafkaSparuralBaseConfig;
import ru.sparural.kafka.consumer.KafkaResponseStatus;
import ru.sparural.kafka.model.KafkaRequestMessage;
import ru.sparural.kafka.model.KafkaResponseMessage;
import ru.sparural.kafka.model.serialization.SerializerUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@Slf4j
public class KafkaSparuralProducerImpl implements KafkaSparuralProducer {

    private final Map<String, KafkaRequestInfo> processingMessages = new HashMap<>();
    private final Boolean generateTraceId;
    private final Integer timeout;
    private final String producerReplyTopic;
    private final KafkaTemplate producerTemplate;
    private final ConsumerFactory<String, KafkaResponseMessage> producerReplyFactory;
    private final SerializerUtils serializerUtils = new SerializerUtils();
    private ConcurrentMessageListenerContainer container = null;

    @PostConstruct
    public void init() {
        ContainerProperties containerProperties = new ContainerProperties(producerReplyTopic);
        containerProperties.setMessageListener((MessageListener<String, KafkaResponseMessage>)
                this::processReplyMessage);

        container = new ConcurrentMessageListenerContainer<>(producerReplyFactory, containerProperties);
        container.start();
    }

    @PreDestroy
    public void unload() {
        container.stop();
    }

    @Override
    public KafkaRequestInfo sendAsync(String topic, String action) {
        return sendAsync(Collections.emptyMap(), topic, action, null);
    }

    @Override
    public KafkaRequestInfo sendAsync(String topic, String action, Object payload) {
        return sendAsync(Collections.emptyMap(), topic, action, payload);
    }

    @Override
    public KafkaRequestInfo sendAsync(Map<String, Object> params, String topic, String action) {
        return sendAsync(params, topic, action, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public KafkaRequestInfo sendAsync(Map<String, Object> params, String topic,
                                      String action, @Nullable Object payload) {
        KafkaResponseMessage response;
        String correlationId = UUID.randomUUID().toString();
        String traceId = MDC.get(KafkaSparuralBaseConfig.MDC_TRACE_ID_KEY);

        try {
            KafkaRequestMessage request = new KafkaRequestMessage();
            request.setAction(action);
            request.setCorrelationId(correlationId.getBytes());

            if (traceId == null) {
                traceId = UUID.randomUUID().toString();
                MDC.put(KafkaSparuralBaseConfig.MDC_TRACE_ID_KEY, traceId);
            }

            request.setTraceId(traceId.getBytes());
            if (payload != null)
                request.setPayload(serializerUtils.setPayload(payload, null));
            request.setParams(params);
            request.setReplyTopic(producerReplyTopic);
            KafkaRequestInfo requestInfo = new KafkaRequestInfo(
                    correlationId,
                    traceId,
                    new CompletableFuture<>(), timeout,
                    action,
                    topic
            );
            ProducerRecord<String, KafkaRequestMessage> record = new ProducerRecord<>(
                    topic, UUID.randomUUID().toString(), request);

            producerTemplate.send(record);
            processingMessages.put(correlationId, requestInfo);
            log.info("Sent action '{}' for topic '{} with correlationId '{}'",
                    action, topic, correlationId);
            return requestInfo;
        } catch (JsonProcessingException | RuntimeException ex) {
            log.error("Error during produce message", ex);
            response = new KafkaResponseMessage();
            response.setStatus(KafkaResponseStatus.CLIENT_SIDE_ERROR);
            response.setPayload(ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        }
    }

    @Override
    public KafkaResponseMessage send(Map<String, Object> params, String topic, String action, Object payload) {
        try {
            KafkaRequestInfo requestInfo = sendAsync(params, topic, action, payload);
            var resp = requestInfo.getFuture().get(timeout, TimeUnit.SECONDS);
            MDC.remove(KafkaSparuralBaseConfig.MDC_TRACE_ID_KEY);
            return resp;
        } catch (TimeoutException ex) {
            log.warn("Kafka consumer not answer");
            KafkaResponseMessage response = new KafkaResponseMessage();
            response.setStatus(KafkaResponseStatus.TIMEOUT);
            response.setPayload("request timeout");
            return response;
        } catch (InterruptedException | ExecutionException ex) {
            log.error("Error during produce message", ex);
            KafkaResponseMessage response = new KafkaResponseMessage();
            response.setStatus(KafkaResponseStatus.CLIENT_SIDE_ERROR);
            response.setPayload(ex.getLocalizedMessage());
            return response;
        }
    }

    @Override
    public KafkaResponseMessage send(String topic, String action, Object payload) {
        return send(Collections.emptyMap(), topic, action, payload);
    }

    @Override
    public KafkaResponseMessage send(String topic, String action) {
        return send(topic, action, Collections.emptyMap());
    }

    @Override
    public KafkaResponseMessage send(Map<String, Object> params, String topic, String action) {
        return send(params, topic, action, null);
    }

    private void processReplyMessage(ConsumerRecord<String, KafkaResponseMessage> message) {
        byte[] correlationIdBytes = message.value().getCorrelationId();
        if (correlationIdBytes == null) {
            return;
        }
        String correlationId = new String(correlationIdBytes);
        KafkaRequestInfo info = processingMessages.remove(correlationId);
        if (info == null) {
            return;
        }
        log.info("Producer timelog action '{}' for topic '{}' at correlationId {} processed at {}ms",
                info.getAction(),
                info.getTopic(),
                info.getCorrelationId(),
                System.currentTimeMillis() - info.getCreatedAt());
        info.getFuture().complete(message.value());
    }

}
