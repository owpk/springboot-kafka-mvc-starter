package ru.owpk.kafkamvc.model.serialization.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.owpk.kafkamvc.model.serialization.PayloadSerializer;

public class JsonPayloadSerializer implements PayloadSerializer {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object payload) throws JsonProcessingException {
        return mapper.writeValueAsBytes(payload);
    }

    @Override
    public Object deserializeClass(byte[] payload, String payloadClassName) throws StreamReadException, DatabindException, IOException {
        var javaType = mapper.getTypeFactory().constructFromCanonical(payloadClassName);
        return mapper.readValue(payload, javaType);
    }

    @Override
    public <T> T deserializeClass(byte[] payload, Class<T> className) throws StreamReadException, DatabindException, IOException {
        return mapper.readValue(payload, className);
    }
}
