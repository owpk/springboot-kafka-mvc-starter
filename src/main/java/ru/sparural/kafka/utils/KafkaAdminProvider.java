package ru.sparural.kafka.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Collections;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;

/**
 * @author Vorobyev Vyacheslav
 */
public class KafkaAdminProvider {
    private final String bootstrapServer;
    private KafkaAdmin kafkaAdmin;

    public KafkaAdminProvider(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    public KafkaAdminProvider(KafkaAdmin kafkaAdmin, String bootstrapServer) {
        this.kafkaAdmin = kafkaAdmin;
        this.bootstrapServer = bootstrapServer;
    }

    public void initKafkaAdmin(ApplicationContext applicationContext) {
        this.kafkaAdmin = new KafkaAdmin(Map.of(BOOTSTRAP_SERVERS_CONFIG, bootstrapServer));
        kafkaAdmin.setApplicationContext(applicationContext);
        kafkaAdmin.initialize();
    }

    public void createOrModifyTopic(String topic, int partitionCount, int replicaCount, Map<String, String> configs) {
        var newTopic = TopicBuilder.name(topic)
                .partitions(partitionCount)
                .replicas(replicaCount)
                .configs(configs)
                .build();
        kafkaAdmin.createOrModifyTopics(newTopic);
    }

    public void createOrModifyTopic(String topic, int partitionCount) {
        createOrModifyTopic(topic, partitionCount, 0, Collections.emptyMap());
    }
}