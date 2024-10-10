package ru.sparural.kafka.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import ru.sparural.kafka.KafkaMvcBaseConfig;
import ru.sparural.kafka.KafkaMvcConsumerAutoconfiguration;

/**
 * @author Vyacheslav Vorobev
 */
@Import({KafkaMvcConsumerAutoconfiguration.class, KafkaMvcBaseConfig.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableKafkaMvcConsumer {
}