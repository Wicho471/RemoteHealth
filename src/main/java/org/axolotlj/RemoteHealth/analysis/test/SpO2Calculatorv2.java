package org.axolotlj.RemoteHealth.analysis.test;

import java.util.ArrayList;
import java.util.List;

public class SpO2Calculatorv2 {
	private static final double SPO2_INTERCEPT = 104.0;
	private static final double SPO2_SLOPE = -14.29;
    private static final int SAMPLE_RATE_HZ = 150; // 100 Hz
    private static final int WINDOW_SECONDS = 5;  // segundos de datos para cálculo
    private static final int WINDOW_SIZE = SAMPLE_RATE_HZ * WINDOW_SECONDS;

    private final List<Double> irBuffer = new ArrayList<>();
    private final List<Double> redBuffer = new ArrayList<>();

    public double add(Double irValue, Double redValue, long timestamp) {
        irBuffer.add(irValue);
        redBuffer.add(redValue);

        // Mantener ventana fija
        if (irBuffer.size() > WINDOW_SIZE) {
            irBuffer.remove(0);
            redBuffer.remove(0);
        }

        // Si tenemos suficientes datos, podemos calcular el SpO2
        if (irBuffer.size() == WINDOW_SIZE) {
            return Math.min(100, calculateSpO2(irBuffer, redBuffer));
        } else {
        	return -1;
        }
    }

    private double calculateSpO2(List<Double> ir, List<Double> red) {

        double irMean = ir.stream().mapToDouble(i -> i).average().orElse(0.0);
        double redMean = red.stream().mapToDouble(i -> i).average().orElse(0.0);

        double irAC = calculateACComponent(ir, irMean);
        double redAC = calculateACComponent(red, redMean);

        double r = (redAC / redMean) / (irAC / irMean);

        // Fórmula de estimación de SpO₂ (empírica)
        return SPO2_INTERCEPT + SPO2_SLOPE * r;
    }

    private double calculateACComponent(List<Double> signal, double mean) {
        return Math.sqrt(
            signal.stream()
                  .mapToDouble(v -> v - mean)
                  .map(v -> v * v)
                  .average()
                  .orElse(0.0)
        );
    }
}
