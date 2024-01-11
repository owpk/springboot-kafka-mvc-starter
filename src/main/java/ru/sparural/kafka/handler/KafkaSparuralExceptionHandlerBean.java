package ru.sparural.kafka.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import ru.sparural.kafka.annotation.ExceptionHandler;
import ru.sparural.kafka.annotation.KafkaSparuralExceptionHandler;
import ru.sparural.kafka.model.KafkaRequestMessage;
import ru.sparural.kafka.model.KafkaResponseMessage;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class KafkaSparuralExceptionHandlerBean {

    private final ApplicationContext ctx;
    private final List<ExceptionHandlerEntry> handlers = new ArrayList<>();

    @PostConstruct
    public void init() {
        ctx.getBeansWithAnnotation(KafkaSparuralExceptionHandler.class).forEach((beanName, bean) ->
                processHandler(bean));
    }

    private void processHandler(Object bean) {
        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(ExceptionHandler.class)) {
                if (!method.getReturnType().equals(KafkaResponseMessage.class)) {
                    log.warn("Exception handler must return KafkaResponseMessage: {}: {}", bean.getClass().getName(), method.getName());
                    continue;
                }
                ExceptionHandler handlerAnnotation = method.getAnnotation(ExceptionHandler.class);
                ExceptionHandlerEntry entry = new ExceptionHandlerEntry(bean, method, handlerAnnotation.value());
                handlers.add(entry);
            }
        }
    }

    private int compareException(Class<?> handled, Class<?> processing) {
        int result = 0;
        Class<?> current = handled;
        while (!Object.class.equals(current)) {
            if (current.equals(processing)) {
                return result;
            }
            current = current.getSuperclass();
            result++;
        }
        return -1;
    }

    public KafkaResponseMessage handleException(Exception exception, KafkaRequestMessage request) {
        ExceptionHandlerEntry handler = null;
        var actualException = exception.getCause() == null ? exception : exception.getCause();
        int currentLength = -1;
        for (ExceptionHandlerEntry entry : handlers) {
            int length = compareException(actualException.getClass(), entry.getExceptionClass());
            if (length >= 0 && (length < currentLength || currentLength < 0)) {
                currentLength = length;
                handler = entry;
            }
        }

        if (handler == null) {
            return null;
        }

        Parameter[] parameters = handler.getHandler().getParameters();
        Object[] values = new Object[parameters.length];

        for (int idx = 0; idx < parameters.length; idx++) {
            Parameter param = parameters[idx];
            if (actualException.getClass().isAssignableFrom(param.getType())) {
                values[idx] = exception.getCause();
            } else if (param.getType().equals(KafkaRequestMessage.class)) {
                values[idx] = request;
            } else {
                values[idx] = null;
            }
        }

        try {
            return (KafkaResponseMessage) handler.getHandler().invoke(handler.getBean(), values);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            log.error("Error handle exception", ex);
            return null;
        }
    }
}
