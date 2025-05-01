package org.axolotlj.RemoteHealth.model;

public class SensorValue<T> {
    public enum Status {
        VALID, NOT_READY, ERROR, MALFORMED
    }

    private final T value;
    private final Status status;

    public SensorValue(T value, Status status) {
        this.value = value;
        this.status = status;
    }

    public static <T> SensorValue<T> valid(T value) {
        return new SensorValue<>(value, Status.VALID);
    }

    public static <T> SensorValue<T> notReady() {
        return new SensorValue<>(null, Status.NOT_READY);
    }

    public static <T> SensorValue<T> error() {
        return new SensorValue<>(null, Status.ERROR);
    }
    
    public static <T> SensorValue<T> malformed() {
        return new SensorValue<>(null, Status.MALFORMED);
    }

    public T getValue() {
        return value;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isValid() {
        return status == Status.VALID;
    }

    @Override
    public String toString() {
        return isValid() ? String.valueOf(value) : status.name();
    }
}
