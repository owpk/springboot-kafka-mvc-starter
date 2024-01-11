package ru.sparural.kafka.consumer;

import java.util.Objects;

class ControllerEntryKey {

    private final String topic;
    private final String action;

    public ControllerEntryKey(String topic, String action) {
        this.topic = topic;
        this.action = action;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.topic);
        hash = 29 * hash + Objects.hashCode(this.action);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ControllerEntryKey other = (ControllerEntryKey) obj;
        if (!Objects.equals(this.topic, other.topic)) {
            return false;
        }
        return Objects.equals(this.action, other.action);
    }
}
