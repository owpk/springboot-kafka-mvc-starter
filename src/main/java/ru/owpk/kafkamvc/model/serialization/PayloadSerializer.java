package ru.owpk.kafkamvc.model.serialization;

import java.util.Map;

public interface PayloadSerializer {

    byte[] serialize(Object payload) throws Exception;
    Object deserializeClass(byte[] payload, String className) throws Exception;
    <T> T deserializeClass(byte[] payload, Class<T> className) throws Exception;

    byte[] serializeFromMap(Map<String, Object> payload) throws Exception;
    Map<String, Object> deserializeToMap(byte[] payload) throws Exception;
}
