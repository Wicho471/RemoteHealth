package org.axolotlj.RemoteHealth.analysis.spo2;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.github.psambit9791.jdsp.signal.peaks.FindPeak;
import com.github.psambit9791.jdsp.signal.peaks.Peak;

/**
 * Procesador de señales PPG (IR y RED) para estimar SpO₂.
 */
public class Spo2Monitor {

	private final Deque<Double> redBuf = new ArrayDeque<>();
	private final Deque<Double> irBuf = new ArrayDeque<>();
	private final Deque<Long> timeBuf = new ArrayDeque<>();
	private final Deque<Double> spo2Buf = new ArrayDeque<>();

	private final double spo2A;
	private final double spo2B;
	private final double spo2WindowSec;
	private volatile double fs;
	
	private int sampleCounter = 0;

	private Consumer<ImmutablePair<Long, Integer>> listener;

	/**
	 * Crea una nueva instancia de {@code Spo2Processor} con parámetros
	 * personalizados.
	 *
	 * @param spo2A         Constante A del modelo de cálculo de SpO2.
	 * @param spo2B         Constante B del modelo de cálculo de SpO2.
	 * @param fs            Frecuencia de muestreo (Hz).
	 * @param spo2WindowSec Duración de la ventana de análisis para SpO2 (en
	 *                      segundos).
	 */
	public Spo2Monitor(double spo2A, double spo2B, double fs, double spo2WindowSec, Consumer<ImmutablePair<Long, Integer>> listener) {
		this.spo2A = spo2A;
		this.spo2B = spo2B;
		this.fs = fs;
		this.spo2WindowSec = spo2WindowSec;
		this.listener = listener;
	}

	/**
	 * Crea una nueva instancia de {@code Spo2Processor} con valores por defecto:
	 * <ul>
	 * <li>{@code spo2A = 110.0}</li>
	 * <li>{@code spo2B = 25.0}</li>
	 * <li>{@code fs = 100} Hz</li>
	 * <li>{@code spo2WindowSec = 6.0} segundos</li>
	 * </ul>
	 * Estos valores están preconfigurados para un entorno de procesamiento estándar
	 * de SpO2.
	 */
	public Spo2Monitor(Consumer<ImmutablePair<Long, Integer>> listener) {
		this(110.0, 25.0, 100.0, 6.0, listener);
	}

	/**
	 * Procesa una muestra PPG y devuelve el SpO₂ si está disponible.
	 *
	 * @param ir  Señal IR.
	 * @param red Señal RED.
	 * @param tMs Marca de tiempo en milisegundos.
	 * @return SpO₂ como OptionalDouble.
	 */
	public void addPlethSample(double ir, double red, long tMs) {
		irBuf.addLast(ir);
		redBuf.addLast(red);
		timeBuf.addLast(tMs);

		purgeOld(tMs);

	    if (timeBuf.size() < fs || sampleCounter++ % 5 != 0)
	        return;

		double[] irArr = irBuf.stream().mapToDouble(Double::doubleValue).toArray();
		double[] redArr = redBuf.stream().mapToDouble(Double::doubleValue).toArray();

		Peak irPeaks = new FindPeak(irArr).detectPeaks();
		Peak redPeaks = new FindPeak(redArr).detectPeaks();

		if (irPeaks.getPeaks().length < 2 || redPeaks.getPeaks().length < 2)
			return;

		double acIr = peakToPeak(irArr, irPeaks.getPeaks());
		double dcIr = mean(irArr);
		double acRed = peakToPeak(redArr, redPeaks.getPeaks());
		double dcRed = mean(redArr);

		if (dcIr == 0 || dcRed == 0)
			return;

		double R = (acRed / dcRed) / (acIr / dcIr);
		double spo2 = spo2A - spo2B * R;
		double clamped = Math.max(0, Math.min(100, spo2));
		spo2Buf.addLast(clamped);
		if (spo2Buf.size() > 256)
			spo2Buf.removeFirst();
		double[] spo2Arr = spo2Buf.stream().mapToDouble(Double::doubleValue).toArray();
		listener.accept(ImmutablePair.of(tMs, Math.min(100, ((int) mean(spo2Arr) + 6))));
	}

	private void purgeOld(long now) {
		long winMs = (long) (spo2WindowSec * 1000);
		while (!timeBuf.isEmpty() && now - timeBuf.peekFirst() > winMs) {
			timeBuf.removeFirst();
			irBuf.removeFirst();
			redBuf.removeFirst();
		}
	}

	private static double peakToPeak(double[] d, int[] idx) {
		double max = Arrays.stream(idx).mapToDouble(i -> d[i]).max().orElse(0);
		double min = Arrays.stream(idx).mapToDouble(i -> d[i]).min().orElse(0);
		return max - min;
	}

	private static double mean(double[] a) {
		return Arrays.stream(a).average().orElse(0);
	}

	public void setFs(double fs) {
		this.fs = fs;
	}
	
	public void stop() {
	    irBuf.clear();
	    redBuf.clear();
	    timeBuf.clear();
	    spo2Buf.clear();
	    listener = spo2 -> {};
	    listener = null;
	}
}
