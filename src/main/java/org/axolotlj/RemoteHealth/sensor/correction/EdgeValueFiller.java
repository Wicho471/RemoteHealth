package org.axolotlj.RemoteHealth.sensor.correction;

import java.util.List;

import org.axolotlj.RemoteHealth.sensor.data.DataPoint;
import org.axolotlj.RemoteHealth.sensor.data.SensorValue;
import org.axolotlj.RemoteHealth.sensor.handle.SensorField;

/**
 * Rellena los valores NR al inicio y final de la serie con el valor válido más cercano.
 */
public class EdgeValueFiller {

    public void fillEdges(List<DataPoint> dataList, SensorField field) {
        if (dataList == null || dataList.isEmpty()) return;

        SensorValue<?> firstValid = findFirstValidForward(dataList, field);
        if (firstValid != null) {
            for (DataPoint data : dataList) {
                if (!SensorFieldAccessor.get(data, field).isValid()) {
                    SensorFieldAccessor.set(data, field, firstValid.copy());
                } else {
                    break;
                }
            }
        }

        SensorValue<?> lastValid = findLastValidBackward(dataList, field);
        if (lastValid != null) {
            for (int i = dataList.size() - 1; i >= 0; i--) {
                if (!SensorFieldAccessor.get(dataList.get(i), field).isValid()) {
                    SensorFieldAccessor.set(dataList.get(i), field, lastValid.copy());
                } else {
                    break;
                }
            }
        }
    }

    private SensorValue<?> findFirstValidForward(List<DataPoint> dataList, SensorField field) {
        for (DataPoint data : dataList) {
            SensorValue<?> value = SensorFieldAccessor.get(data, field);
            if (value.isValid()) return value;
        }
        return null;
    }

    private SensorValue<?> findLastValidBackward(List<DataPoint> dataList, SensorField field) {
        for (int i = dataList.size() - 1; i >= 0; i--) {
            SensorValue<?> value = SensorFieldAccessor.get(dataList.get(i), field);
            if (value.isValid()) return value;
        }
        return null;
    }
}
