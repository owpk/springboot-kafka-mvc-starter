package ru.sparural.kafka.consumer;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum KafkaResponseStatus {
    SUCCESS {{
        code = 200;
    }},
    FAILURE {{
        code = 400;
    }},
    SERVER_ERROR {{
        code = 500;
    }},
    MULTIPLE_CHOOSE {{
        code = 300;
    }},
    NOT_FOUND {{
        code = 404;
    }},
    INVALID_RESPONSE,
    CLIENT_SIDE_ERROR,
    TIMEOUT {{
        code = 408;
    }},
    INVALID_REQUEST {{
        code = 400;
    }},
    STATUS_CODE;

    int code;

    public static KafkaResponseStatus valueOf(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst().orElse(STATUS_CODE.status(code));
    }

    public KafkaResponseStatus status(Integer code) {
        this.code = code;
        return this;
    }

    @JsonValue
    public int getCode() {
        return code;
    }
}