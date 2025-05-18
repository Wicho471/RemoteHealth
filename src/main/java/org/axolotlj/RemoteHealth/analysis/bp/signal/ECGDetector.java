package org.axolotlj.RemoteHealth.analysis.bp.signal;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Detecta picos R en señal ECG filtrada.
 */
public class ECGDetector {

    private static final int REFRACTORY_MS = 300;
    private static final double THRESHOLD_K = 0.70;

    private final DescriptiveStatistics stats = new DescriptiveStatistics(1000);
    private int lastValue = 0;
    private long lastPeak = -REFRACTORY_MS;

    public boolean onSample(long timestamp, int value) {
        stats.addValue(value);
        double threshold = stats.getMean() + THRESHOLD_K * stats.getStandardDeviation();

        boolean isPeak = value > threshold && lastValue <= threshold &&
                timestamp - lastPeak > REFRACTORY_MS;

        lastValue = value;
        if (isPeak) lastPeak = timestamp;
        return isPeak;
    }
}