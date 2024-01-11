package ru.sparural.kafka.annotation;


import org.springframework.context.annotation.Import;
import ru.sparural.kafka.KafkaSparuralBaseConfig;
import ru.sparural.kafka.KafkaSparuralConsumerAutoconfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vyacheslav Vorobev
 */
@Import({KafkaSparuralConsumerAutoconfiguration.class, KafkaSparuralBaseConfig.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableSparuralKafkaConsumer {
}