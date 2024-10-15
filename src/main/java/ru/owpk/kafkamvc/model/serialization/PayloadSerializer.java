package ru.owpk.kafkamvc.model.serialization;

public interface PayloadSerializer {

    byte[] serialize(Object payload) throws Exception;

    Object deserializeClass(byte[] payload, String className) throws Exception;
    <T> T deserializeClass(byte[] payload, Class<T> className) throws Exception;
}
