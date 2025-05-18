package org.axolotlj.RemoteHealth.sensor.correction;

import org.axolotlj.RemoteHealth.sensor.data.DataPoint;
import org.axolotlj.RemoteHealth.sensor.data.SensorValue;
import org.axolotlj.RemoteHealth.sensor.handle.SensorField;

/**
 * Utilidad para acceder y modificar dinámicamente campos de sensores.
 */
public class SensorFieldAccessor {

    @SuppressWarnings("unchecked")
    public static <T> SensorValue<T> get(DataPoint data, SensorField field) {
        return switch (field) {
            case ECG -> (SensorValue<T>) data.getEcg();
            case ACCEL -> (SensorValue<T>) data.getAccel();
            case TEMP -> (SensorValue<T>) data.getTemp();
            case IR -> (SensorValue<T>) data.getIr();
            case RED -> (SensorValue<T>) data.getRed();
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> void set(DataPoint data, SensorField field, SensorValue<T> value) {
        switch (field) {
            case ECG -> data.setEcg((SensorValue<Short>) value);
            case ACCEL -> data.setAccel((SensorValue<Float>) value);
            case TEMP -> data.setTemp((SensorValue<Float>) value);
            case IR -> data.setIr((SensorValue<Integer>) value);
            case RED -> data.setRed((SensorValue<Integer>) value);
        }
    }
}
