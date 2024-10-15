package ru.owpk.kafkamvc.model.serialization.impl;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import ru.owpk.kafkamvc.model.serialization.PayloadSerializer;

public class BinaryPayloadSerializer implements PayloadSerializer {

    @Override
    public byte[] serialize(Object payload) throws Exception {
        final LinkedBuffer BUFFER = LinkedBuffer.allocate();
        Schema schema = RuntimeSchema.getSchema(payload.getClass());
        return ProtostuffIOUtil.toByteArray(payload, schema, BUFFER);
    }

    @Override
    public Object deserializeClass(byte[] payload, String className) throws Exception {
        Class<?> clazz = Class.forName(className);
        Schema schema = RuntimeSchema.getSchema(clazz);
        var javaObject = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(payload, javaObject, schema);
        return javaObject;
    }

    @Override
    public <T> T deserializeClass(byte[] payload, Class<T> className) throws Exception {
        Schema<T> schema = RuntimeSchema.getSchema(className);
        var javaObject = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(payload, javaObject, schema);
        return javaObject;
    }

}
