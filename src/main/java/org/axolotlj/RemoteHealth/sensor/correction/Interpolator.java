package org.axolotlj.RemoteHealth.sensor.correction;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

import org.axolotlj.RemoteHealth.sensor.data.DataPoint;
import org.axolotlj.RemoteHealth.sensor.data.SensorValue;
import org.axolotlj.RemoteHealth.sensor.handle.SensorField;

/**
 * Interpola valores NR internos utilizando interpolación cúbica spline.
 */
public class Interpolator {

    public void interpolate(List<DataPoint> dataList, SensorField field) {
        List<Integer> knownIndices = new ArrayList<>();
        List<Double> knownValues = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            SensorValue<?> value = SensorFieldAccessor.get(dataList.get(i), field);
            if (value.isValid()) {
                knownIndices.add(i);
                knownValues.add(((Number) value.getValue()).doubleValue());
            }
        }

        if (knownIndices.size() < 3) return;

        double[] x = knownIndices.stream().mapToDouble(Integer::doubleValue).toArray();
        double[] y = knownValues.stream().mapToDouble(Double::doubleValue).toArray();

        UnivariateFunction splineFunction = new SplineInterpolator().interpolate(x, y);

        for (int i = 0; i < dataList.size(); i++) {
            SensorValue<?> current = SensorFieldAccessor.get(dataList.get(i), field);
            if (!current.isValid()) {
                if (i >= x[0] && i <= x[x.length - 1]) {
                    double interpolated = splineFunction.value(i);
                    setInterpolated(dataList.get(i), field, interpolated);
                }
            }
        }
    }
    
    private void setInterpolated(DataPoint data, SensorField field, double value) {
        switch (field) {
            case ECG -> data.setEcg(SensorValue.valid((short) value));
            case ACCEL -> data.setAccel(SensorValue.valid((float) value));
            case TEMP -> data.setTemp(SensorValue.valid((float) value));
            case IR -> data.setIr(SensorValue.valid((int) value));
            case RED -> data.setRed(SensorValue.valid((int) value));
        }
    }
}