package org.axolotlj.RemoteHealth.sensor.handle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.axolotlj.RemoteHealth.sensor.data.DataPoint;
import org.axolotlj.RemoteHealth.sensor.data.SensorValue;

/**
 * Clase encargada de extraer diferentes formas de datos desde una lista de DataPoint.
 * Permite obtener arreglos de valores simples, pares válidos o pares crudos con sus timestamps.
 */
public class DataExtractor {

	/**
	 * Extrae un arreglo de valores numéricos del tipo especificado para el campo indicado.
	 *
	 * @param dataList lista de datos.
	 * @param field campo sensor a extraer.
	 * @param clazz clase del tipo de dato (Float.class, Integer.class, etc.)
	 * @param <T> tipo de dato numérico
	 * @return arreglo de valores del tipo T.
	 */
	public static <T extends Number> T[] extractNumericValues(List<DataPoint> dataList, SensorField field, Class<T> clazz) {
	    @SuppressWarnings("unchecked")
	    T[] result = (T[]) Array.newInstance(clazz, dataList.size());

	    for (int i = 0; i < dataList.size(); i++) {
	        SensorValue<?> sensorValue = getSensorValue(dataList.get(i), field);
	        Object value = sensorValue.getValue();
	        result[i] = clazz.cast(value);
	    }

	    return result;
	}


    /**
     * Extrae pares (timestamp, valor) donde los valores sean válidos.
     *
     * @param dataList lista de datos.
     * @param field campo sensor a extraer.
     * @return lista de pares con timestamp y valor double.
     */
    public static ArrayList<MutablePair<Long, Double>> extractValidValues(List<DataPoint> dataList, SensorField field) {
    	ArrayList<MutablePair<Long, Double>> pairs = new ArrayList<>();

        for (DataPoint data : dataList) {
            SensorValue<?> sensorValue = getSensorValue(data, field);

            if (sensorValue.isValid()) {
                Number value = (Number) sensorValue.getValue();
                pairs.add(MutablePair.of(data.getTimeStamp(), value.doubleValue()));
            }
        }

        return pairs;
    }

    /**
     * Extrae pares (timestamp, valor crudo) sin filtrar por validez.
     *
     * @param dataList lista de datos.
     * @param field campo sensor a extraer.
     * @param <T> tipo de dato del valor del sensor.
     * @return lista de pares con timestamp y valor SensorValue<T>.
     */
    public static <T> List<MutablePair<Long, SensorValue<T>>> extractRawValues(List<DataPoint> dataList, SensorField field) {
        List<MutablePair<Long, SensorValue<T>>> pairs = new ArrayList<>();

        for (DataPoint data : dataList) {
            SensorValue<?> rawValue = getSensorValue(data, field);
            @SuppressWarnings("unchecked")
            SensorValue<T> typedValue = (SensorValue<T>) rawValue;
            pairs.add(MutablePair.of(data.getTimeStamp(), typedValue));
        }

        return pairs;
    }
    
    public static ArrayList<MutableTriple<Long, Double, Double>> extractValidValues(
            List<DataPoint> dataList, SensorField field1, SensorField field2) {
        
        ArrayList<MutableTriple<Long, Double, Double>> triples = new ArrayList<>();

        for (DataPoint data : dataList) {
            SensorValue<?> value1 = getSensorValue(data, field1);
            SensorValue<?> value2 = getSensorValue(data, field2);

            if (value1.isValid() && value2.isValid()) {
                double v1 = ((Number) value1.getValue()).doubleValue();
                double v2 = ((Number) value2.getValue()).doubleValue();
                triples.add(MutableTriple.of(data.getTimeStamp(), v1, v2));
            }
        }

        return triples;
    }

    public static <T> List<MutableTriple<Long, SensorValue<T>, SensorValue<T>>> extractRawValues(
            List<DataPoint> dataList, SensorField field1, SensorField field2) {
        
        List<MutableTriple<Long, SensorValue<T>, SensorValue<T>>> triples = new ArrayList<>();

        for (DataPoint data : dataList) {
            SensorValue<?> raw1 = getSensorValue(data, field1);
            SensorValue<?> raw2 = getSensorValue(data, field2);

            @SuppressWarnings("unchecked")
            SensorValue<T> typed1 = (SensorValue<T>) raw1;
            @SuppressWarnings("unchecked")
            SensorValue<T> typed2 = (SensorValue<T>) raw2;

            triples.add(MutableTriple.of(data.getTimeStamp(), typed1, typed2));
        }

        return triples;
    }

    private static SensorValue<?> getSensorValue(DataPoint data, SensorField field) {
        return switch (field) {
            case ECG -> data.getEcg();
            case ACCEL -> data.getAccel();
            case TEMP -> data.getTemp();
            case IR -> data.getIr();
            case RED -> data.getRed();
        };
    }
}
