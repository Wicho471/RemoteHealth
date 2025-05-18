package org.axolotlj.RemoteHealth.filters.core;

import com.github.psambit9791.jdsp.filter.FIRWin1;
import com.github.psambit9791.jdsp.filter.FIRWin1.FIRfilterType;
import com.github.psambit9791.jdsp.signal.Convolution;



/**
 * Clase que implementa filtros FIR (fase lineal) usando la librería FIRWin1.
 * Cada función modifica in-place el ArrayList original y retorna una nueva
 * lista filtrada.
 */
public class FIRFiltering {

	/**
	 * Aplica un filtro FIR Low-Pass (pasa-bajas).
	 *
	 * @param signal      Amplitud.
	 * @param fs          Frecuencia de muestreo (Hz).
	 * @param numTaps     Número de coeficientes (orden FIR + 1).
	 * @param cutoffHz    Frecuencia de corte en Hz (ej. 40.0).
	 * @param transition  Ancho de transición en Hz (ej. 5.0).
	 * @param scaleCoeffs Si true, escala los coeficientes a ganancia 1 en la banda
	 *                    de paso.
	 *
	 * @return Nueva lista con la señal filtrada. (El original se modifica
	 *         in-place).
	 */
	public static double[]  applyFIRLowPass(double[] signal,
			double fs, int numTaps, double cutoffHz, double transition, boolean scaleCoeffs) {

		// 2) Instanciar la clase FIRWin1 con numTaps y transitionWidth
		// Ejemplo: new FIRWin1(numTaps, transition, fs)
		// => La "width" se interpretará en la constructora como ancho de transición
		FIRWin1 firDesigner = new FIRWin1(numTaps, transition, fs);

		// 3) Definir el array de cutoff (solo 1 valor para LOWPASS)
		double[] cutoff = new double[] { cutoffHz };

		// 4) Calcular coeficientes con computeCoefficients
		double[] coeffs = firDesigner.computeCoefficients(cutoff, FIRfilterType.LOWPASS, scaleCoeffs);

		// 5) Convolucionar la señal
		Convolution conv = new Convolution(signal, coeffs);
		double[] filtered = conv.convolve();

		return filtered;
	}

	/**
	 * Aplica un filtro FIR High-Pass (pasa-altas).
	 *
	 * @param signal        Lista ECG (amplitud, tiempo).
	 * @param fs          Frecuencia de muestreo (Hz).
	 * @param numTaps     Número de coeficientes (orden FIR + 1).
	 * @param cutoffHz    Frecuencia de corte en Hz (ej. 0.5).
	 * @param transition  Ancho de transición en Hz (ej. 0.2).
	 * @param scaleCoeffs Escalar coeficientes a 1 en la banda de paso si es true.
	 *
	 * @return Nueva lista con la señal filtrada. (El original se modifica
	 *         in-place).
	 */
	public static double[]  applyFIRHighPass(double[] signal,
			double fs, int numTaps, double cutoffHz, double transition, boolean scaleCoeffs) {

		FIRWin1 firDesigner = new FIRWin1(numTaps, transition, fs);

		// Para HIGHPASS, cutoff[] = un solo valor
		double[] cutoff = new double[] { cutoffHz };

		double[] coeffs = firDesigner.computeCoefficients(cutoff, FIRfilterType.HIGHPASS, scaleCoeffs);

		Convolution conv = new Convolution(signal, coeffs);
		double[] filtered = conv.convolve();

		return filtered;
	}

	/**
	 * Aplica un filtro FIR Band-Pass (pasa-banda).
	 *
	 * @param data        Lista ECG (amplitud, tiempo).
	 * @param fs          Frecuencia de muestreo (Hz).
	 * @param numTaps     Número de coeficientes (orden FIR + 1).
	 * @param lowCutHz    Frecuencia de corte baja (Hz).
	 * @param highCutHz   Frecuencia de corte alta (Hz).
	 * @param transition  Ancho de transición (Hz). Usado para la ventana Kaiser.
	 * @param scaleCoeffs Escalar coeficientes a 1 en la banda de paso si es true.
	 *
	 * @return Nueva lista con la señal filtrada. (El original se modifica
	 *         in-place).
	 */
	public static double[]  applyFIRBandPass(double[] signal,
			double fs, int numTaps, double lowCutHz, double highCutHz, double transition, boolean scaleCoeffs) {

		FIRWin1 firDesigner = new FIRWin1(numTaps, transition, fs);

		// Para BANDPASS, cutoff[] = {lowCut, highCut}
		double[] cutoff = new double[] { lowCutHz, highCutHz };

		double[] coeffs = firDesigner.computeCoefficients(cutoff, FIRfilterType.BANDPASS, scaleCoeffs);

		Convolution conv = new Convolution(signal, coeffs);
		double[] filtered = conv.convolve();

		return filtered;
	}

	/**
	 * Aplica un filtro FIR Band-Stop (rechaza-banda).
	 *
	 * @param data        Lista ECG (amplitud, tiempo).
	 * @param fs          Frecuencia de muestreo (Hz).
	 * @param numTaps     Número de coeficientes (orden FIR + 1).
	 * @param lowCutHz    Frecuencia baja de la banda a rechazar.
	 * @param highCutHz   Frecuencia alta de la banda a rechazar.
	 * @param transition  Ancho de transición (Hz). Usado para la ventana Kaiser.
	 * @param scaleCoeffs Escalar coeficientes a 1 en la banda de paso si es true.
	 *
	 * @return Nueva lista con la señal filtrada. (El original se modifica
	 *         in-place).
	 */
	public static double[] applyFIRBandStop(double[] signal,
			double fs, int numTaps, double lowCutHz, double highCutHz, double transition, boolean scaleCoeffs) {

		FIRWin1 firDesigner = new FIRWin1(numTaps, transition, fs);

		// Para BANDSTOP, cutoff[] = {lowCut, highCut}
		double[] cutoff = new double[] { lowCutHz, highCutHz };

		double[] coeffs = firDesigner.computeCoefficients(cutoff, FIRfilterType.BANDSTOP, scaleCoeffs);

		Convolution conv = new Convolution(signal, coeffs);
		double[] filtered = conv.convolve();

		return filtered;
	}

	// ------------------------------------------------------------------------
	// Si lo deseas, podrías crear métodos análogos para MULTIBANDPASS o
	// MULTIBANDSTOP, recibiendo más de 2 frecuencias en el array cutoff.
	// ------------------------------------------------------------------------
}
