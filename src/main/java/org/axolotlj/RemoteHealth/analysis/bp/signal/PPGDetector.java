package org.axolotlj.RemoteHealth.analysis.bp.signal;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Detecta el "foot" en la señal PPG RED.
 */
public class PPGDetector {

    private static final int REFRACTORY_MS = 400;
    private static final double THRESHOLD_K = 0.20;

    private final DescriptiveStatistics stats = new DescriptiveStatistics(400);
    private int previous = 0;
    private long lastFoot = -REFRACTORY_MS;

    public boolean onSample(long timestamp, int value) {
        int delta = value - previous;
        previous = value;

        stats.addValue(delta);
        double threshold = THRESHOLD_K * stats.getStandardDeviation();

        boolean isFoot = delta > threshold && timestamp - lastFoot > REFRACTORY_MS;
        if (isFoot) lastFoot = timestamp;
        return isFoot;
    }
}
