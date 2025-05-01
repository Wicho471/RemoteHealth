package org.axolotlj.RemoteHealth.filters;

import java.util.Arrays;

public class Misc {
	public static double[] normalize(double[] values) {
	    double max = Arrays.stream(values).max().orElse(1.0);
	    double min = Arrays.stream(values).min().orElse(0.0);
	    double range = max - min;

	    if (range == 0) {
	        System.err.println("⚠️ ECG rango 0: todos los valores iguales.");
	        return values.clone(); // o puedes devolver solo ceros si prefieres
	    }

	    double[] normalized = new double[values.length];
	    double mean = mean(values);            // ✅ CORRECTO
	    double std = std(values, mean);        // ✅ CORRECTO

	    if (std == 0) {
	        System.err.println("⚠️ Desviación estándar 0: datos sin variación.");
	        return values.clone();
	    }

	    for (int i = 0; i < values.length; i++) {
	        normalized[i] = (values[i] - mean) / std;
	    }

	    return normalized;
	}


	/**
	 * Calcula la media de un array de doubles.
	 */
	public static double mean(double[] data) {
		double sum = 0.0;
		for (double v : data) {
			sum += v;
		}
		return sum / data.length;
	}

		public static double calculateAverageSamplingRate(long[] timestamps) {
		    if (timestamps.length < 2) {
		        throw new IllegalArgumentException("Se necesitan al menos 2 timestamps para calcular la tasa de muestreo.");
		    }
	
		    long start = timestamps[0];
		    long end = timestamps[timestamps.length - 1];
		    long durationMillis = end - start;
	
		    if (durationMillis <= 0) return 0.0;
	
		    double durationSeconds = durationMillis / 1000.0;
		    return timestamps.length / durationSeconds;
		}

	/**
	 * Calcula la desviación estándar de un array de doubles, recibiendo la media
	 * precalculada.
	 */
	private static double std(double[] data, double mean) {
		double sumSq = 0.0;
		for (double v : data) {
			double diff = v - mean;
			sumSq += diff * diff;
		}
		return Math.sqrt(sumSq / (data.length - 1));
	}
}
