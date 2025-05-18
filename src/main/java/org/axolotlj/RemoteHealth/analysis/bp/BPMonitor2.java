package org.axolotlj.RemoteHealth.analysis.bp;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.Consumer;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Estimador de presión arterial sistólica y diastólica (SBP/DBP) en tiempo real
 * a partir de señales filtradas de ECG (250 Hz) y PPG RED (100 Hz).
 * <p>
 * Entrada esperada: línea CSV con formato:
 * timestampHex, ecgHex, acc, temp, irHex, redHex
 *
 * ©2025 – Solo para investigación / bienestar.
 */
public class BPMonitor2 {

    private final Deque<Double> sbpWindow = new ArrayDeque<>();
    private final Deque<Double> dbpWindow = new ArrayDeque<>();

    /* === CONSTANTES ===================================================== */
    private static final int REFRACTORY_ECG_MS = 300;
    private static final int REFRACTORY_PPG_MS = 400;
    private static final int PTT_MIN_MS = 90;
    private static final int PTT_MAX_MS = 350;
    private static final double ECG_THRESHOLD_K = 0.70;
    private static final double PPG_THRESHOLD_K = 0.20;
    private static final double ALPHA_HR = 0.20;
    private static final double ALPHA_PTT = 0.20;

    /* Coeficientes calibrables para SBP/DBP */
    private volatile double coeffA = 49, coeffB = 10_000, coeffC = 0.30;
    private volatile double coeffD = 16, coeffE = 10_000, coeffF = 0.20;

    /* === SUBMÓDULOS ===================================================== */
    private final ECGDetector ecgDetector = new ECGDetector();
    private final PPGDetector ppgDetector = new PPGDetector();
    private final BPEstimator estimator = new BPEstimator();
    private final Consumer<BPEstimate> resultListener;

    /**
     * Crea una instancia del monitor de presión con un listener de resultados.
     *
     * @param listener función de callback para consumir las estimaciones
     */
    public BPMonitor2(Consumer<BPEstimate> listener) {
        this.resultListener = listener;
    }

    /**
     * Procesa una línea de entrada CSV con señales filtradas.
     *
     * @param csv línea en formato CSV: timestampHex, ecgHex, acc, temp, irHex, redHex
     */
    public void feedCsvLine(String csv) {
        if (csv == null || csv.isBlank()) return;

        String[] fields = csv.split(",");
        if (fields.length < 6) return;

        long timestampMs = Long.parseLong(fields[0].trim(), 16);
        String ecgHex = fields[1].trim();
        String redHex = fields[5].trim();

        if (!"NR".equals(ecgHex)) {
            int ecgValue = Integer.parseInt(ecgHex, 16);
            if (ecgDetector.onSample(timestampMs, ecgValue)) estimator.onRPeak(timestampMs);
        }

        if (!"NR".equals(redHex)) {
            int redValue = Integer.parseInt(redHex, 16);
            if (ppgDetector.onSample(timestampMs, redValue)) estimator.onPPGFoot(timestampMs);
        }
    }

    /**
     * Actualiza los coeficientes A–F después de la calibración.
     */
    public void setCoefficients(double a, double b, double c, double d, double e, double f) {
        this.coeffA = a; this.coeffB = b; this.coeffC = c;
        this.coeffD = d; this.coeffE = e; this.coeffF = f;
    }

    /**
     * Detector de picos R en la señal de ECG.
     */
    private static class ECGDetector {
        private final DescriptiveStatistics ecgStats = new DescriptiveStatistics(1000);
        private long lastRPeakTimestamp = -REFRACTORY_ECG_MS;
        private int lastSampleValue = 0;

        boolean onSample(long timestamp, int sampleValue) {
            ecgStats.addValue(sampleValue);
            double threshold = ecgStats.getMean() + ECG_THRESHOLD_K * ecgStats.getStandardDeviation();

            boolean isRPeak = sampleValue > threshold
                    && lastSampleValue <= threshold
                    && timestamp - lastRPeakTimestamp > REFRACTORY_ECG_MS;

            lastSampleValue = sampleValue;
            if (isRPeak) lastRPeakTimestamp = timestamp;
            return isRPeak;
        }
    }

    /**
     * Detector del "foot" en la señal PPG del canal RED.
     */
    private static class PPGDetector {
        private final DescriptiveStatistics derivativeStats = new DescriptiveStatistics(400);
        private long lastFootTimestamp = -REFRACTORY_PPG_MS;
        private int previousSample = 0;

        boolean onSample(long timestamp, int sampleValue) {
            int derivative = sampleValue - previousSample;
            previousSample = sampleValue;

            derivativeStats.addValue(derivative);
            double threshold = PPG_THRESHOLD_K * derivativeStats.getStandardDeviation();

            boolean isFoot = derivative > threshold && timestamp - lastFootTimestamp > REFRACTORY_PPG_MS;
            if (isFoot) lastFootTimestamp = timestamp;
            return isFoot;
        }
    }

    /**
     * Estimador de HR, PTT, y BP a partir de eventos ECG y PPG.
     */
    private class BPEstimator {
        private final ArrayDeque<Long> rPeakQueue = new ArrayDeque<>();
        private final ArrayDeque<Long> ppgFootQueue = new ArrayDeque<>();
        private long previousRPeakTimestamp = -1;
        private double ewmaHeartRate = 0;
        private double ewmaPTT = 0;

        void onRPeak(long timestamp) {
            rPeakQueue.add(timestamp);
            tryEmitEstimate();
        }

        void onPPGFoot(long timestamp) {
            ppgFootQueue.add(timestamp);
            tryEmitEstimate();
        }

        private void tryEmitEstimate() {
            while (!rPeakQueue.isEmpty() && !ppgFootQueue.isEmpty()) {
                long rTimestamp = rPeakQueue.peekFirst();
                long footTimestamp = ppgFootQueue.peekFirst();

                if (footTimestamp <= rTimestamp) {
                    ppgFootQueue.pollFirst();
                    continue;
                }

                long ptt = footTimestamp - rTimestamp;
                if (ptt < PTT_MIN_MS || ptt > PTT_MAX_MS) {
                    rPeakQueue.pollFirst();
                    continue;
                }

                long rrInterval = (previousRPeakTimestamp < 0) ? -1 : rTimestamp - previousRPeakTimestamp;
                if (rrInterval > 0 && ptt > 0.6 * rrInterval) {
                    ppgFootQueue.pollFirst();
                    continue;
                }

                rPeakQueue.pollFirst();
                ppgFootQueue.pollFirst();
                previousRPeakTimestamp = rTimestamp;

                double currentHR = (rrInterval <= 0) ? Double.NaN : 60_000.0 / rrInterval;
                if (!Double.isNaN(currentHR) && currentHR >= 35 && currentHR <= 220) {
                    ewmaHeartRate = (ewmaHeartRate == 0) ? currentHR
                            : ALPHA_HR * currentHR + (1 - ALPHA_HR) * ewmaHeartRate;
                } else {
                    continue;
                }

                ewmaPTT = (ewmaPTT == 0) ? ptt : ALPHA_PTT * ptt + (1 - ALPHA_PTT) * ewmaPTT;

                double sbp = coeffA + coeffB / ewmaPTT + coeffC * ewmaHeartRate;
                double dbp = coeffD + coeffE / ewmaPTT + coeffF * ewmaHeartRate;

                sbpWindow.addLast(sbp);
                dbpWindow.addLast(dbp);
                if (sbpWindow.size() > 15) sbpWindow.removeFirst();
                if (dbpWindow.size() > 15) dbpWindow.removeFirst();

                double[] sbpArray = sbpWindow.stream().mapToDouble(Double::doubleValue).toArray();
                double[] dbpArray = dbpWindow.stream().mapToDouble(Double::doubleValue).toArray();

                resultListener.accept(new BPEstimate(
                        computeMean(sbpArray),
                        computeMean(dbpArray),
                        ewmaHeartRate,
                        ewmaPTT
                ));
            }
        }
    }

    private static double computeMean(double[] values) {
        return Arrays.stream(values).average().orElse(0);
    }

    /**
     * Objeto de salida para representar la estimación por latido.
     *
     * @param sbp   Presión sistólica estimada
     * @param dbp   Presión diastólica estimada
     * @param hr    Frecuencia cardíaca en bpm
     * @param pttMs Tiempo de tránsito del pulso (ms)
     */
    public static record BPEstimate(double sbp, double dbp, double hr, double pttMs) {}
}

