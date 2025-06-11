package org.axolotlj.RemoteHealth.filters.core;

import java.awt.desktop.SystemSleepEvent;

import jwave.exceptions.JWaveException;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.Wavelet;
import jwave.transforms.wavelets.WaveletBuilder;

/**
 * Clase para filtrar (denoising) una señal ECG mediante transformada wavelet,
 * utilizando la librería JWave.
 */
public class WaveletDenoiser {
	
	public static final String[] AVAILABLE_WAVELETS = { "Haar", "Haar orthogonal", "Daubechies 2", "Daubechies 3",
			"Daubechies 4", "Daubechies 5", "Daubechies 6", "Daubechies 7", "Daubechies 8", "Daubechies 9",
			"Daubechies 10", "Daubechies 11", "Daubechies 12", "Daubechies 13", "Daubechies 14", "Daubechies 15",
			"Daubechies 16", "Daubechies 17", "Daubechies 18", "Daubechies 19", "Daubechies 20", "Coiflet 1",
			"Coiflet 2", "Coiflet 3", "Coiflet 4", "Coiflet 5", "Legendre 1", "Legendre 2", "Legendre 3", "Symlet 2",
			"Symlet 3", "Symlet 4", "Symlet 5", "Symlet 6", "Symlet 7", "Symlet 8", "Symlet 9", "Symlet 10",
			"Symlet 11", "Symlet 12", "Symlet 13", "Symlet 14", "Symlet 15", "Symlet 16", "Symlet 17", "Symlet 18",
			"Symlet 19", "Symlet 20", "BiOrthogonal 1/1", "BiOrthogonal 1/3", "BiOrthogonal 1/5", "BiOrthogonal 2/2",
			"BiOrthogonal 2/4", "BiOrthogonal 2/6", "BiOrthogonal 2/8", "BiOrthogonal 3/1", "BiOrthogonal 3/3",
			"BiOrthogonal 3/5", "BiOrthogonal 3/7", "BiOrthogonal 3/9", "BiOrthogonal 4/4", "BiOrthogonal 5/5",
			"BiOrthogonal 6/8", "Discrete Mayer" };

	/**
	 * Filtra la señal ECG usando transformada wavelet, umbralizando los
	 * coeficientes de detalle para eliminar ruido de alta frecuencia.
	 * <p>
	 * Esta función modifica in-place la amplitud (left) del ArrayList y retorna una
	 * nueva lista con la señal ya filtrada.
	 * 
	 * @param ecgData     ArrayList con (amplitud, tiempo).
	 * @param waveletName Nombre del wavelet (e.g. "haar", "daub4", "sym8", etc.).
	 * @param levels      Niveles de descomposición (mayor => separa más banda).
	 * @param threshold   Umbral para atenuar/eliminar coeficientes de detalle.
	 *                    (puede ser calculado según ruido esperado o heurística).
	 * @param soft        true = soft-threshold, false = hard-threshold.
	 * 
	 * @return Una nueva lista con la señal filtrada por wavelet denoising.
	 */
	public static double[]  waveletDenoise(double[] signal, String waveletName, int levels, double threshold, boolean soft) throws JWaveException{
		System.out.println("Se mando llamar el metodo");
		// 2) Ajustar la longitud a potencia de 2 (opcional si usas FWT).
		// Si JWave no admite longitud arbitraria en forward(),
		// haz zero-padding a la siguiente potencia de 2. Aquí un ejemplo:
		int origLength = signal.length;
		int newLength = nextPowerOfTwo(origLength);
		double[] padded = new double[newLength];
		System.arraycopy(signal, 0, padded, 0, origLength);
		// El resto queda en 0

		// 3) Crear wavelet y transform
		Wavelet wave = WaveletBuilder.create(waveletName);
		FastWaveletTransform fwt = new FastWaveletTransform(wave);

		// 4) Transformada wavelet hacia adelante (multinivel)
		double[] waveCoeffs = fwt.forward(padded, levels);

		// 5) Umbralizar coeficientes de detalle
		// - El 50% superior del array waveCoeffs (aprox) son los detalles en varios
		// niveles.
		// - Cada nivel de detalle se almacena en segmentos. Simplificamos y
		// umbralizamos
		// TODO menos la 1a posición, que es la aproximación global.
		applyThreshold(waveCoeffs, threshold, soft);

		// 6) Reconstruir la señal
		double[] recovered = fwt.reverse(waveCoeffs, levels);

		if (recovered.length > origLength) {
		    double[] trimmed = new double[origLength];
		    System.arraycopy(recovered, 0, trimmed, 0, origLength);
		    return trimmed;
		}
		
		return recovered;
	}

	/**
	 * Aplica umbralización (threshold) a los coeficientes wavelet a partir de un
	 * índice. Se asume que la aproximación más baja (coefs iniciales) son
	 * intocables si deseas preservar la componente DC. Este ejemplo umbraliza TODOS
	 * los detalles, excepto la componente 0 (la más global).
	 * 
	 * @param data      Arreglo con coeficientes wavelet
	 * @param threshold Valor de umbral
	 * @param soft      true => soft-threshold, false => hard-threshold
	 */
	private static void applyThreshold(double[] data, double threshold, boolean soft) {
		// Empecemos en 1 para no tocar data[0].
		// O podrías ajustar según el wavelet packet si quieres omitir más.
		for (int i = 1; i < data.length; i++) {
			double val = data[i];
			if (Math.abs(val) < threshold) {
				// Hard-threshold => se fuerza a cero
				data[i] = 0.0;
			} else if (soft) {
				// Soft-threshold => reduce además la magnitud
				double sign = (val > 0.0) ? 1.0 : -1.0;
				data[i] = sign * (Math.abs(val) - threshold);
			}
		}
	}

	/**
	 * Encuentra la siguiente potencia de 2 mayor o igual a 'n'.
	 */
	private static int nextPowerOfTwo(int n) {
		int v = 1;
		while (v < n) {
			v <<= 1;
		}
		return v;
	}
}
