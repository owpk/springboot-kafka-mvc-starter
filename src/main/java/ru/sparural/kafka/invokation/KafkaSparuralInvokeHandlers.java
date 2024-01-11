package ru.sparural.kafka.invokation;

import lombok.Data;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import ru.sparural.kafka.model.KafkaRequestMessage;

@Data
public class KafkaSparuralInvokeHandlers {

    private final ThreadLocal<KafkaRequestMessage> currentRequest = new ThreadLocal<>();

    public KafkaRequestMessage kafkaRequestMessage() {
        MethodInterceptor handler = (obj, method ,  args,  proxy) -> {
            KafkaRequestMessage request = currentRequest.get();
            return (proxy.invoke(request, args));
        };
        return (KafkaRequestMessage) Enhancer.create(KafkaRequestMessage.class, handler);
    }

    public void setCurrentThreadRequest(KafkaRequestMessage request) {
        currentRequest.set(request);
    }
}
