package ru.sparural.kafka.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
public class ExceptionHandlerEntry {
    private Object bean;
    private Method handler;
    private Class<? extends Exception> exceptionClass;
}
