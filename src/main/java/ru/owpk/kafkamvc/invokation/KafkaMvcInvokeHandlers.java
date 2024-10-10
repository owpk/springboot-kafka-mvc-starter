package ru.owpk.kafkamvc.invokation;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

import lombok.Data;
import ru.owpk.kafkamvc.model.KafkaRequestMessage;

@Data
public class KafkaMvcInvokeHandlers {

    private final ThreadLocal<KafkaRequestMessage> currentRequest = new ThreadLocal<>();

    public KafkaRequestMessage kafkaRequestMessage() {
        MethodInterceptor handler = (obj, method, args, proxy) -> {
            KafkaRequestMessage request = currentRequest.get();
            return (proxy.invoke(request, args));
        };
        return (KafkaRequestMessage) Enhancer.create(KafkaRequestMessage.class, handler);
    }

    public void setCurrentThreadRequest(KafkaRequestMessage request) {
        currentRequest.set(request);
    }
}
