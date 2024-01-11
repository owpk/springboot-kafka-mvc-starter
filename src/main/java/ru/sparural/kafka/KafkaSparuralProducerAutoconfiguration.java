package ru.sparural.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.*;
import ru.sparural.kafka.model.KafkaRequestMessage;
import ru.sparural.kafka.model.KafkaResponseMessage;
import ru.sparural.kafka.model.serialization.KafkaRequestSerializer;
import ru.sparural.kafka.model.serialization.KafkaResponseDeserializer;
import ru.sparural.kafka.producer.KafkaSparuralProducer;
import ru.sparural.kafka.producer.KafkaSparuralProducerImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

@Slf4j
@RequiredArgsConstructor
public class KafkaSparuralProducerAutoconfiguration {

    @Value("${sparural.kafka.bootstrap-servers}")
    private String serviceName;

    @Value("${sparural.kafka.producer.generateTraceId:false}")
    private Boolean generateTraceId;

    @Value("${sparural.kafka.producer.replyTopic}")
    private String producerReplyTopic;

    @Value("${sparural.kafka.producer.timeout:5}")
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
    public DefaultKafkaConsumerFactory<String, KafkaResponseMessage> producerReplyFactory(KafkaResponseDeserializer kafkaResponseDeserializer) {
        Map<String, Object> consumerConfig = Map.of(
                BOOTSTRAP_SERVERS_CONFIG, serviceName,
                GROUP_ID_CONFIG, UUID.randomUUID().toString() // Important to be random!
        );
        return new DefaultKafkaConsumerFactory<>(
                consumerConfig, new StringDeserializer(), kafkaResponseDeserializer);
    }

    @Bean
    public KafkaTemplate<String, KafkaRequestMessage> producerTemplate(ProducerFactory<String, KafkaRequestMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaSparuralProducer kafkaSparuralProducer(KafkaTemplate<String, KafkaRequestMessage> producerTemplate, ConsumerFactory<String, KafkaResponseMessage> producerReplyFactory) {
        return new KafkaSparuralProducerImpl(generateTraceId, timeout, producerReplyTopic, producerTemplate, producerReplyFactory);
    }
}
