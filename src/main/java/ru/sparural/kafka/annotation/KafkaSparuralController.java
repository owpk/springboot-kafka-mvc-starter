package ru.sparural.kafka.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Component
public @interface KafkaSparuralController {

    String value() default "";

    /**
     * topic name
     * spring property format allowed
     */
    String topic();

    /**
     * idle interval between kafka consumer poll in millis
     */
    String idleInterval() default "0";
}