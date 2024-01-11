package ru.sparural.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.spi.MDCAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.PropertyResolver;
import org.springframework.kafka.core.*;
import ru.sparural.kafka.consumer.KafkaSparuralConsumer;
import ru.sparural.kafka.consumer.RequestGateway;
import ru.sparural.kafka.handler.KafkaSparuralExceptionHandlerBean;
import ru.sparural.kafka.model.KafkaRequestMessage;
import ru.sparural.kafka.model.KafkaResponseMessage;
import ru.sparural.kafka.model.serialization.KafkaRequestDeserializer;
import ru.sparural.kafka.model.serialization.KafkaResponseSerializer;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import ru.sparural.kafka.invokation.KafkaSparuralInvokeHandlers;

@Slf4j
@RequiredArgsConstructor
public class KafkaSparuralConsumerAutoconfiguration {

    private final PropertyResolver propertyResolver;
    @Value("${sparural.kafka.bootstrap-servers}")
    private String serviceName;
    @Value("${sparural.kafka.consumer.name}")
    private String consumerName;
    @Value("${sparural.kafka.consumer.threads.max:50}")
    private Integer consumerMaxThreadPoolCount;
    @Value("${sparural.kafka.consumer.threads.start:10}")
    private Integer consumerStartThreadPoolCount;

    @Bean
    public String kafkaConsumerId() {
        return consumerName; // Important not to be random!
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, KafkaRequestMessage> kafkaConsumerFactory(
            String consumerName, KafkaRequestDeserializer kafkaRequestDeserializer) {
        log.debug("Connect to kafka server: {}", serviceName);
        Map<String, Object> consumerConfig = Map.of(
                BOOTSTRAP_SERVERS_CONFIG, serviceName,
                GROUP_ID_CONFIG, consumerName
        );
        return new DefaultKafkaConsumerFactory<>(
                consumerConfig, new StringDeserializer(), kafkaRequestDeserializer);
    }

    @Bean
    public KafkaSparuralConsumer kafkaSparuralConsumer(ApplicationContext applicationCtx,
                                                       ConsumerFactory<String, KafkaRequestMessage> kafkaConsumerFactory,
                                                       KafkaTemplate<String, KafkaResponseMessage> replyTemplate,
                                                       KafkaSparuralExceptionHandlerBean kafkaSparuralExceptionHandlerBean,
                                                       KafkaSparuralInvokeHandlers kafkaSparuralInvokeHandlers) {
        var requestGateway = new RequestGateway("SPARURAL_KAFKA_CONSUMER:",
                consumerStartThreadPoolCount, consumerMaxThreadPoolCount);
        return new KafkaSparuralConsumer(applicationCtx, kafkaConsumerFactory, replyTemplate,
                kafkaSparuralExceptionHandlerBean, propertyResolver, requestGateway, kafkaSparuralInvokeHandlers);
    }

    @Bean
    public KafkaSparuralExceptionHandlerBean kafkaSparuralExceptionHandlerBean(ApplicationContext applicationCtx) {
        return new KafkaSparuralExceptionHandlerBean(applicationCtx);
    }

    @Bean
    public ProducerFactory<String, KafkaResponseMessage> replyProducerFactory(KafkaResponseSerializer kafkaResponseSerializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serviceName);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var producerFactory = new DefaultKafkaProducerFactory<String, KafkaResponseMessage>(props);
        producerFactory.setValueSerializer(kafkaResponseSerializer);
        return producerFactory;
    }

    @Bean
    public KafkaTemplate<String, KafkaResponseMessage> replyTemplate(ProducerFactory<String, KafkaResponseMessage> replyProducerFactory) {
        return new KafkaTemplate<>(replyProducerFactory);
    }

    @Bean
    public KafkaSparuralInvokeHandlers kafkaSparuralInvokeHandlers() {
        return new KafkaSparuralInvokeHandlers();
    }

    @Bean
    public KafkaRequestMessage kafkaRequestMessage(KafkaSparuralInvokeHandlers kafkaSparuralInvokeHandlers) {
        return kafkaSparuralInvokeHandlers.kafkaRequestMessage();
    }
}
