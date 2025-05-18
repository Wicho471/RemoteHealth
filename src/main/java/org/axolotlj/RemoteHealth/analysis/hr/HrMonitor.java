package org.axolotlj.RemoteHealth.analysis.hr;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 * Procesador de señales ECG para estimar el ritmo cardíaco (BPM).
 */
public class HrMonitor {
	
	private final Deque<Double> integBuf = new ArrayDeque<>();
	private final Deque<Long> rPeakTimes = new ArrayDeque<>();
	private double prevSample = 0;
	private double integSum = 0;
	private final int integWin;
	private double sPk = 0, nPk = 0, thr = 0;
	private final double beatsInWindow;
	private Consumer<ImmutablePair<Long, Integer>> listener;

	/**
	 * Crea una nueva instancia de {@code BpmProcessor} con parámetros
	 * personalizados.
	 *
	 * @param fs            Frecuencia de muestreo (Hz), usada para calcular la
	 *                      ventana de integración.
	 * @param beatsInWindow Número de latidos (beats) considerados dentro de la
	 *                      ventana de análisis para calcular BPM.
	 */
	public HrMonitor(double fs, double beatsInWindow, Consumer<ImmutablePair<Long, Integer>> listener) {
		this.integWin = (int) Math.round(0.15 * fs); // Ventana de integración de 150 ms
		this.beatsInWindow = beatsInWindow;
		this.listener = listener;
	}

	/**
	 * Crea una nueva instancia de {@code BpmProcessor} con valores por defecto:
	 * <ul>
	 * <li>Frecuencia de muestreo: {@code fs = 275} Hz</li>
	 * <li>Ventana de integración: {@code 0.15 * fs = 41} muestras</li>
	 * <li>Latidos considerados: {@code beatsInWindow = 5}</li>
	 * </ul>
	 * Estos valores están pensados para entornos típicos de procesamiento de señal
	 * cardíaca.
	 */
	public HrMonitor(Consumer<ImmutablePair<Long, Integer>> listener) {
		this.integWin = (int) Math.round(0.15 * 250);
		this.beatsInWindow = 5;
		this.listener = listener;
	}

	/**
	 * Procesa una muestra de ECG y devuelve el BPM si está disponible.
	 *
	 * @param ecg Valor filtrado de la muestra de ECG.
	 * @param tMs Marca de tiempo en milisegundos.
	 * @return BPM como OptionalDouble.
	 */
	public void addEcgSample(double ecg, long tMs) {
		double der = ecg - prevSample;
		prevSample = ecg;
		double energy = der * der;

		integBuf.addLast(energy);
		integSum += energy;
		if (integBuf.size() > integWin)
			integSum -= integBuf.removeFirst();
		double integ = integSum / integBuf.size();

		boolean isPeak = integ > thr;
		updateThresholds(integ, isPeak);

		if (!isPeak && !rPeakTimes.isEmpty()) {
			long last = rPeakTimes.peekLast();
			long delta = tMs - last;
			double estRR = estimateMedianRR();
			if (estRR > 0 && delta > 1.66 * estRR)
				thr *= 0.5;
		}

		if (isPeak && isNewBeat(tMs)) {
			rPeakTimes.addLast(tMs);
			if (rPeakTimes.size() > beatsInWindow + 1)
				rPeakTimes.removeFirst();
		}

		if (rPeakTimes.size() < 2)
			return;
		double medRR = estimateMedianRR();
		double bpm = 60_000.0 / medRR;
		listener.accept(ImmutablePair.of(tMs, (int)Math.round(bpm)));
	}

	private boolean isNewBeat(long tMs) {
		if (rPeakTimes.isEmpty())
			return true;
		long rr = tMs - rPeakTimes.peekLast();
		return rr > 300;
	}

	private double estimateMedianRR() {
		int k = rPeakTimes.size();
		if (k < 2)
			return -1;
		double[] rr = new double[k - 1];
		Long[] ts = rPeakTimes.toArray(new Long[0]);
		for (int i = 1; i < k; i++)
			rr[i - 1] = ts[i] - ts[i - 1];
		return new Median().evaluate(rr);
	}

	private void updateThresholds(double integ, boolean isPeak) {
		if (isPeak)
			sPk = 0.125 * integ + 0.875 * sPk;
		else
			nPk = 0.125 * integ + 0.875 * nPk;
		thr = nPk + 0.25 * (sPk - nPk);
	}
	
	public void stop() {
	    integBuf.clear();
	    rPeakTimes.clear();
	    listener = bpm -> {};
	    listener = null;
	}
}
