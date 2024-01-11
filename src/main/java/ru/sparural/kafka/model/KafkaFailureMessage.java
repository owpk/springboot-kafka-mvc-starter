package ru.sparural.kafka.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
public class KafkaFailureMessage {
    private String message;
    private List<String> arguments;

    public KafkaFailureMessage(String message, Object... args) {
        this.message = message;
        this.arguments = Stream.of(args).map(Object::toString).collect(Collectors.toList());
    }


}
