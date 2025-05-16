package org.axolotlj.RemoteHealth.analysis;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.MutablePair;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class FrequencyDomainAnalyzer {

	/**
	 * Calcula la FFT single-sided (frecuencia vs. magnitud normalizada) de la señal
	 * dada en ecgData.
	 * 
	 * @param ecgData Lista de pares (amplitud, tiempo)
	 * @param FS      Frecuencia de muestreo en Hz
	 * @return ArrayList de (frecuencia, magnitud)
	 */
	public static ArrayList<MutablePair<Double, Double>> computeFFT(double[] signal,
			double FS) {
		// ==========================
		// 1) Copiar señal a array
		// ==========================
		long startTime = System.currentTimeMillis();
		int n = signal.length;
		long endTime = System.currentTimeMillis();
		System.out.println("Copiar elementos: " + (endTime - startTime) + " ms");

		// Opcional: si quieres forzar potencia de 2 para más velocidad:
		// int newN = nextPowerOfTwo(n);
		// if (newN != n) {
		// signal = java.util.Arrays.copyOf(signal, newN);
		// n = newN;
		// }

		// ==========================
		// 2) Preparar array in-place para FFT
		// JTransforms, para la FFT real, la maneja
		// con un array doble (real + imag intercalado)
		// si usas DoubleFFT_1D.realForwardFull(...).
		// O con realForward(...) si sólo necesitas la mitad.
		// ==========================
		startTime = System.currentTimeMillis();

		// Convertimos la señal real en un array de longitud n (doble).
		// Pero usaremos realForwardFull() y la salida será de longitud 2*n,
		// la parte par = real, la parte impar = imaginaria.
		double[] fftData = new double[2 * n];
		System.arraycopy(signal, 0, fftData, 0, n);

		// Instanciar la FFT
		DoubleFFT_1D fft = new DoubleFFT_1D(n);

		// Realizamos la FFT de una señal real en "modo full" in-place
		fft.realForwardFull(fftData);

		endTime = System.currentTimeMillis();
		System.out.println("FFT (JTransforms) tiempo: " + (endTime - startTime) + " ms");

		// ==========================
		// 3) Calcular magnitudes single-sided
		// ==========================
		startTime = System.currentTimeMillis();

		// halfSize = n/2 (si n es par) + 1, en general
		int halfSize = (n % 2 == 0) ? (n / 2 + 1) : (n + 1) / 2;
		double[] magnitudes = new double[halfSize];

		// Indices en fftData:
		// - Real en índice 2k
		// - Imag en índice 2k+1
		// Donde 0 <= k < n.
		// Para la parte "single-sided", k va de 0..halfSize-1.
		for (int k = 0; k < halfSize; k++) {
			double re = fftData[2 * k];
			double im = fftData[2 * k + 1];
			magnitudes[k] = Math.sqrt(re * re + im * im);
		}

		// Normalizar magnitudes con respecto al máximo
		double maxVal = 0.0;
		for (double val : magnitudes) {
			if (val > maxVal)
				maxVal = val;
		}
		if (maxVal < 1e-12) {
			maxVal = 1e-12;
		}
		for (int i = 0; i < magnitudes.length; i++) {
			magnitudes[i] /= maxVal;
		}

		endTime = System.currentTimeMillis();
		System.out.println("Calcular magnitud + normalizar: " + (endTime - startTime) + " ms");

		// ==========================
		// 4) Construir eje de frecuencias
		// single-sided:
		// freq[k] = k * (Fs / n), para 0 <= k < halfSize
		// ==========================
		startTime = System.currentTimeMillis();

		double[] freqAxis = new double[halfSize];
		for (int k = 0; k < halfSize; k++) {
			freqAxis[k] = k * ((double) FS / (double) n);
		}

		// ==========================
		// 5) Empaquetar resultado en ArrayList de pares
		// ==========================
		ArrayList<MutablePair<Double, Double>> result = new ArrayList<>(halfSize);
		for (int i = 0; i < halfSize; i++) {
			// ✅ AHORA (bien)
			result.add(new MutablePair<>(freqAxis[i], magnitudes[i]));

		}

		endTime = System.currentTimeMillis();
		System.out.println("Generar objeto de retorno: " + (endTime - startTime) + " ms");

		return result;
	}
}
