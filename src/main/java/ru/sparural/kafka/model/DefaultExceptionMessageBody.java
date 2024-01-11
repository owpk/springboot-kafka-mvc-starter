package ru.sparural.kafka.model;

import lombok.*;

/**
 * @author Vorobyev Vyacheslav
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DefaultExceptionMessageBody {
    private Boolean success;
    private String message;
}
