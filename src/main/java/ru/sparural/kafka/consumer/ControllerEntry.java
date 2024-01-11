package ru.sparural.kafka.consumer;

import java.lang.reflect.Method;

class ControllerEntry {

    private final Object bean;
    private final Method method;

    public ControllerEntry(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

}
