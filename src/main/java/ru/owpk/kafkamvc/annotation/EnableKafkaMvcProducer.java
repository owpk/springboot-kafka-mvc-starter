package ru.owpk.kafkamvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import ru.owpk.kafkamvc.KafkaMvcBaseConfig;
import ru.owpk.kafkamvc.KafkaMvcProducerAutoconfiguration;

@Import({KafkaMvcProducerAutoconfiguration.class, KafkaMvcBaseConfig.class})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableKafkaMvcProducer {

}
