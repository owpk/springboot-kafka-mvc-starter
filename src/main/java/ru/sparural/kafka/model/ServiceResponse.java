package ru.sparural.kafka.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * @author Vorobyev Vyacheslav
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceResponse {
    private String respondent;
    private Boolean success;
    private Integer code;
    private String message;
    private Object body;
    private Object meta;
}