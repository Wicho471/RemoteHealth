package org.axolotlj.RemoteHealth.analysis.test;

/**
 * Detector sencillo de BPM basado en threshold y tiempo real del sistema.
 */
public class BpmCalculator {

    private final double threshold;
    private final long minIntervalMs;  // mínimo intervalo en milisegundos entre latidos

    private long lastPeakTime = -1;
    private double lastBpm = 0.0;

    public BpmCalculator(double threshold, double minIntervalSeconds) {
        this.threshold = threshold;
        this.minIntervalMs = (long) (minIntervalSeconds * 1000);
    }

    /**
     * Procesa un nuevo valor y calcula BPM si detecta un latido.
     *
     * @param value El valor actual de la señal (ECG, IR, etc.).
     * @return El BPM calculado si se detecta un latido, o -1 si no hay nuevo latido.
     */
    public double processSample(double value) {
        long now = System.currentTimeMillis();

        if (value > threshold) {
            if (lastPeakTime < 0 || (now - lastPeakTime) >= minIntervalMs) {
                if (lastPeakTime > 0) {
                    long intervalMs = now - lastPeakTime;
                    lastBpm = 60000.0 / intervalMs;
                }
                lastPeakTime = now;
                return lastBpm;
            }
        }

        return -1; // No nuevo latido detectado
    }

    /**
     * Devuelve el último BPM detectado (aunque no haya uno nuevo todavía).
     */
    public double getLastBpm() {
        return lastBpm;
    }
}
