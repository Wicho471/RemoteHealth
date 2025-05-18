package org.axolotlj.RemoteHealth.analysis.bp.core;

import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.axolotlj.RemoteHealth.analysis.bp.signal.ECGDetector;
import org.axolotlj.RemoteHealth.analysis.bp.signal.PPGDetector;

/**
 * Monitor en tiempo real de presión arterial (SBP/DBP) basado en ECG y PPG RED.
 */
public class BPMonitor {

    private final ECGDetector ecgDetector = new ECGDetector();
    private final PPGDetector ppgDetector = new PPGDetector();
    private final BPEstimator estimator;

    /**
     * Constructor principal que recibe un listener para los resultados.
     *
     * @param listener consumidor de estimaciones de presión
     */
    public BPMonitor(Consumer<ImmutableTriple<Long ,Double, Double>> listener) {
        this.estimator = new BPEstimator(listener);
    }

    /**
     * Procesa una línea CSV con datos hexadecimales filtrados.
     *
     * @param csv Línea de entrada CSV: timestampHex, ecgHex, acc, temp, irHex, redHex
     */
    public void feedCsvLine(String csv) {
        if (csv == null || csv.isBlank()) return;

        String[] fields = csv.split(",");
        if (fields.length < 6) return;

        long timestamp = Long.parseLong(fields[0].trim(), 16);
        String ecgHex = fields[1].trim();
        String redHex = fields[5].trim();

        if (!"NR".equals(ecgHex)) {
            int ecgValue = Integer.parseInt(ecgHex, 16);
            if (ecgDetector.onSample(timestamp, ecgValue)) estimator.onRPeak(timestamp);
        }

        if (!"NR".equals(redHex)) {
            int redValue = Integer.parseInt(redHex, 16);
            if (ppgDetector.onSample(timestamp, redValue)) estimator.onPPGFoot(timestamp);
        }
    }

    /**
     * Permite ajustar los coeficientes de calibración.
     */
    public void setCoefficients(double a, double b, double c, double d, double e, double f) {
        estimator.setCoefficients(a, b, c, d, e, f);
    }
    
    public void stop() {
        estimator.stop();
    }
}
