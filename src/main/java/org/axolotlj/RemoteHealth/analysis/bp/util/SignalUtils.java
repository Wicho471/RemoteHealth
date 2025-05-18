package org.axolotlj.RemoteHealth.analysis.bp.util;

import java.util.Collection;

public class SignalUtils {
    public static double mean(Collection<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
}
