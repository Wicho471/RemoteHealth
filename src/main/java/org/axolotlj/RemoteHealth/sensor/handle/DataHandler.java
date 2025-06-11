package org.axolotlj.RemoteHealth.sensor.handle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;

public class DataHandler {

    /**
     * Extrae un subconjunto de datos dentro de una ventana temporal específica.
     *
     * @param data     Lista original de pares tiempo-valor
     * @param initial  Tiempo inicial relativo en milisegundos desde el primer punto
     * @param duration Duración en milisegundos del intervalo que se desea extraer
     * @return Lista filtrada de pares dentro del intervalo especificado
     */
    public static ArrayList<MutablePair<Long, Double>> zoomPairs(List<MutablePair<Long, Double>> data, int initial, int duration) {

    	ArrayList<MutablePair<Long, Double>> zoomed = new ArrayList<>();

        if (data == null || data.isEmpty()) return zoomed;

        try {
            long referenceTime = data.get(0).getLeft();
            long start = referenceTime + initial;
            long end = start + duration;

            for (MutablePair<Long, Double> pair : data) {
                long timestamp = pair.getLeft();
                if (timestamp >= start && timestamp <= end) {
                    zoomed.add(pair);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en DataZoomProcessor.zoomPairs: " + e.getMessage());
            return zoomed;
        }

        return zoomed;
    }
    
    /**
     * Extrae un subconjunto de datos dentro de una ventana temporal específica.
     *
     * @param data     Lista original de pares tiempo-valor
     * @param initial  Tiempo inicial relativo en milisegundos desde el primer punto
     * @param duration Duración en milisegundos del intervalo que se desea extraer
     * @return Lista filtrada de pares dentro del intervalo especificado
     */
    public static ArrayList<MutableTriple<Long, Double, Double>> zoomTriple(ArrayList<MutableTriple<Long, Double, Double>> data, int initial, int duration) {

    	ArrayList<MutableTriple<Long, Double, Double>> zoomed = new ArrayList<>();

        if (data == null || data.isEmpty()) return zoomed;

        try {
            long referenceTime = data.get(0).getLeft();
            long start = referenceTime + initial;
            long end = start + duration;

            for (MutableTriple<Long, Double, Double> triple : data) {
                long timestamp = triple.getLeft();
                if (timestamp >= start && timestamp <= end) {
                    zoomed.add(triple);
                }
            }

        } catch (Exception e) {
            System.err.println("Error en DataZoomProcessor.zoomTriple: " + e.getMessage());
            return zoomed;
        }

        return zoomed;
    }
}