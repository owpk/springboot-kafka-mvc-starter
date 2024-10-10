package ru.owpk.kafkamvc;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.owpk.kafkamvc.model.KafkaRequestMessage;
import ru.owpk.kafkamvc.model.KafkaResponseMessage;
import ru.owpk.kafkamvc.model.serialization.KafkaRequestSerializer;
import ru.owpk.kafkamvc.model.serialization.KafkaResponseDeserializer;
import ru.owpk.kafkamvc.producer.KafkaMvcProducer;
import ru.owpk.kafkamvc.producer.KafkaMvcProducerImpl;

@Slf4j
@RequiredArgsConstructor
public class KafkaMvcProducerAutoconfiguration {

    @Value("${kafka-mvc.bootstrap-servers}")
    private String serviceName;

    @Value("${kafka-mvc.producer.generateTraceId:false}")
    private Boolean generateTraceId;

    @Value("${kafka-mvc.producer.replyTopic}")
    private String producerReplyTopic;

    @Value("${kafka-mvc.producer.timeout:5}")
    private Integer timeout;

    @Bean
    public ProducerFactory<String, KafkaRequestMessage> producerFactory(KafkaRequestSerializer kafkaRequestSerializer) {
        log.debug("Connect to kafka server: {}", serviceName);
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serviceName);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var producerFactory = new DefaultKafkaProducerFactory<String, KafkaRequestMessage>(props);
        producerFactory.setValueSerializer(kafkaRequestSerializer);
        return producerFactory;
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, KafkaResponseMessage> producerReplyFactory(
            KafkaResponseDeserializer kafkaResponseDeserializer) {
        Map<String, Object> consumerConfig = Map.of(
                BOOTSTRAP_SERVERS_CONFIG, serviceName,
                GROUP_ID_CONFIG, UUID.randomUUID().toString() // Important to be random!
        );
        return new DefaultKafkaConsumerFactory<>(
                consumerConfig, new StringDeserializer(), kafkaResponseDeserializer);
    }

    @Bean
    public KafkaTemplate<String, KafkaRequestMessage> producerTemplate(
            ProducerFactory<String, KafkaRequestMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaMvcProducer kafkaMvcProducer(KafkaTemplate<String, KafkaRequestMessage> producerTemplate,
            ConsumerFactory<String, KafkaResponseMessage> producerReplyFactory) {
        return new KafkaMvcProducerImpl(generateTraceId, timeout, producerReplyTopic, producerTemplate,
                producerReplyFactory);
    }
}
