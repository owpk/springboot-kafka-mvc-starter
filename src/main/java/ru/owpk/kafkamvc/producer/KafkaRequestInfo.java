package ru.owpk.kafkamvc.producer;

import java.util.concurrent.CompletableFuture;

import lombok.Getter;
import ru.owpk.kafkamvc.model.KafkaResponseMessage;

@Getter
public class KafkaRequestInfo {
    private final String correlationId;
    private final String traceId;
    private final String action;
    private final String topic;
    private final CompletableFuture<KafkaResponseMessage> future;
    private final long createdAt;
    private final long expireAt;

    public KafkaRequestInfo(String correlationId, String traceId,
            CompletableFuture<KafkaResponseMessage> future,
            int timeout,
            String action,
            String topic) {
        this.correlationId = correlationId;
        this.traceId = traceId;
        this.future = future;
        this.createdAt = System.currentTimeMillis();
        this.expireAt = this.createdAt + timeout * 1000L;
        this.action = action;
        this.topic = topic;
    }
}
