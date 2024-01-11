package ru.sparural.kafka.model.serialization;

public enum HeaderEnum {
    ACTION("action"),
    CORRELATION_ID("kafka_correlationId"),
    TRACE_ID("kafka_traceId"),
    PAYLOAD_TYPE("__TypeId__"),
    REPLY_TOPIC("kafka_replyTopic"),
    STATUS("status"),
    RESPONDENT("respondent"),
    REQUESTER("requester"),
    PARAMS("sparural.params");

    private final String header;

    HeaderEnum(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
