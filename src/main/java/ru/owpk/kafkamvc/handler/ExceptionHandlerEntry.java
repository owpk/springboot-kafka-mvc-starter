package ru.owpk.kafkamvc.handler;

import java.lang.reflect.Method;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionHandlerEntry {
    private Object bean;
    private Method handler;
    private Class<? extends Exception> exceptionClass;
}
