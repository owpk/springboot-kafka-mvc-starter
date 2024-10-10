package ru.sparural.kafka;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.PropertyResolver;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sparural.kafka.consumer.KafkaMvcConsumer;
import ru.sparural.kafka.consumer.RequestGateway;
import ru.sparural.kafka.handler.KafkaMvcExceptionHandlerBean;
import ru.sparural.kafka.invokation.KafkaMvcInvokeHandlers;
import ru.sparural.kafka.model.KafkaRequestMessage;
import ru.sparural.kafka.model.KafkaResponseMessage;
import ru.sparural.kafka.model.serialization.KafkaRequestDeserializer;
import ru.sparural.kafka.model.serialization.KafkaResponseSerializer;

@Slf4j
@RequiredArgsConstructor
public class KafkaMvcConsumerAutoconfiguration {

    private final PropertyResolver propertyResolver;
    @Value("${kafka-mvc.bootstrap-servers}")
    private String serviceName;
    @Value("${kafka-mvc.consumer.name}")
    private String consumerName;
    @Value("${kafka-mvc.consumer.threads.max:50}")
    private Integer consumerMaxThreadPoolCount;
    @Value("${kafka-mvc.consumer.threads.start:10}")
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
                GROUP_ID_CONFIG, consumerName);
        return new DefaultKafkaConsumerFactory<>(
                consumerConfig, new StringDeserializer(), kafkaRequestDeserializer);
    }

    @Bean
    public KafkaMvcConsumer kafkaMvcConsumer(ApplicationContext applicationCtx,
            ConsumerFactory<String, KafkaRequestMessage> kafkaConsumerFactory,
            KafkaTemplate<String, KafkaResponseMessage> replyTemplate,
            KafkaMvcExceptionHandlerBean kafkaMvcExceptionHandlerBean,
            KafkaMvcInvokeHandlers kafkaMvcInvokeHandlers) {
        var requestGateway = new RequestGateway("KAFKA_MVC_CONSUMER:",
                consumerStartThreadPoolCount, consumerMaxThreadPoolCount);
        return new KafkaMvcConsumer(applicationCtx, kafkaConsumerFactory, replyTemplate,
                kafkaMvcExceptionHandlerBean, propertyResolver, requestGateway, kafkaMvcInvokeHandlers);
    }

    @Bean
    public KafkaMvcExceptionHandlerBean kafkaMvcExceptionHandlerBean(ApplicationContext applicationCtx) {
        return new KafkaMvcExceptionHandlerBean(applicationCtx);
    }

    @Bean
    public ProducerFactory<String, KafkaResponseMessage> replyProducerFactory(
            KafkaResponseSerializer kafkaResponseSerializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serviceName);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        var producerFactory = new DefaultKafkaProducerFactory<String, KafkaResponseMessage>(props);
        producerFactory.setValueSerializer(kafkaResponseSerializer);
        return producerFactory;
    }

    @Bean
    public KafkaTemplate<String, KafkaResponseMessage> replyTemplate(
            ProducerFactory<String, KafkaResponseMessage> replyProducerFactory) {
        return new KafkaTemplate<>(replyProducerFactory);
    }

    @Bean
    public KafkaMvcInvokeHandlers kafkaMvcInvokeHandlers() {
        return new KafkaMvcInvokeHandlers();
    }

    @Bean
    public KafkaRequestMessage kafkaRequestMessage(KafkaMvcInvokeHandlers kafkaMvcInvokeHandlers) {
        return kafkaMvcInvokeHandlers.kafkaRequestMessage();
    }
}
