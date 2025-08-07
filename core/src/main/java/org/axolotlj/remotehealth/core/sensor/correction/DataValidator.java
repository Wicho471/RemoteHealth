package org.axolotlj.remotehealth.core.sensor.correction;

import java.util.List;

import org.axolotlj.remotehealth.core.sensor.data.DataPoint;

/**
 * Verifica que todos los datos en una lista sean completamente válidos.
 */
public class DataValidator {

    public boolean isValid(List<DataPoint> dataPoints) {
        for (DataPoint dataPoint : dataPoints) {
            if (!dataPoint.getAccel().isValid()) return false;
            if (!dataPoint.getEcg().isValid()) return false;
            if (!dataPoint.getIr().isValid()) return false;
            if (!dataPoint.getRed().isValid()) return false;
            if (!dataPoint.getTemp().isValid()) return false;
        }
        return true;
    }
}
