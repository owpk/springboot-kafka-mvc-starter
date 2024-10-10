package ru.owpk.kafkamvc;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import lombok.RequiredArgsConstructor;
import ru.owpk.kafkamvc.model.serialization.KafkaRequestDeserializer;
import ru.owpk.kafkamvc.model.serialization.KafkaRequestSerializer;
import ru.owpk.kafkamvc.model.serialization.KafkaResponseDeserializer;
import ru.owpk.kafkamvc.model.serialization.KafkaResponseSerializer;
import ru.owpk.kafkamvc.model.serialization.impl.KafkaRequestDeserializerImpl;
import ru.owpk.kafkamvc.model.serialization.impl.KafkaRequestSerializerImpl;
import ru.owpk.kafkamvc.model.serialization.impl.KafkaResponseDeserializerImpl;
import ru.owpk.kafkamvc.model.serialization.impl.KafkaResponseSerializerImpl;
import ru.owpk.kafkamvc.utils.KafkaAdminProvider;

/**
 * @author Vorobyev Vyacheslav
 */
@RequiredArgsConstructor
public class KafkaMvcBaseConfig {
    public static final String MDC_TRACE_ID_KEY = "trace_id";
    public static final String MDC_CORRELATION_ID_KEY = "correlation_id";
    public static String KAFKA_CLIENT_IDENTIFIER;

    static {
        try {
            KAFKA_CLIENT_IDENTIFIER = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            KAFKA_CLIENT_IDENTIFIER = System.getenv().get("HOSTNAME");
        }
    }

    @Value("${sparural.kafka.bootstrap-servers}")
    private String serviceName;

    @Bean
    public KafkaAdminProvider kafkaAdminProvider() {
        return new KafkaAdminProvider(serviceName);
    }

    @Bean
    public KafkaRequestDeserializer kafkaRequestDeserializer() {
        return new KafkaRequestDeserializerImpl();
    }

    @Bean
    public KafkaResponseSerializer kafkaResponseSerializer() {
        return new KafkaResponseSerializerImpl();
    }

    @Bean
    public KafkaRequestSerializer kafkaRequestSerializer() {
        return new KafkaRequestSerializerImpl();
    }

    @Bean
    public KafkaResponseDeserializer kafkaResponseDeserializer() {
        return new KafkaResponseDeserializerImpl();
    }
}