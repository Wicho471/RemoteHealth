package org.axolotlj.RemoteHealth.analysis.test;

import java.util.ArrayDeque;

/**
 * Calculador estable de SpO₂: ventana temporal + suavizado EMA (sin debounce).
 */
public final class SpO2Calculator {

	/* ----- parámetros ajustables ----- */
	private final long windowMs; // ancho de ventana (p. ej. 6000 ms)
	private final int minSamples; // mínimo de muestras (p. ej. 30)
	private final double alpha; // factor EMA 0‑1 (p. ej. 0.2)
	private final double perfusionMin; // AC/DC mínimo (p. ej. 0.5)

	private static final double A = 110.0; // coef. curva empírica
	private static final double B = 25.0;

	/* ----- ventana deslizante ----- */
	private static final class S {
		double v;
		long t;

		S(double v, long t) {
			this.v = v;
			this.t = t;
		}
	}

	private final ArrayDeque<S> irQ = new ArrayDeque<>();
	private final ArrayDeque<S> redQ = new ArrayDeque<>();

	/* acumulados para DC y AC */
	private double sumIr = 0, sumRed = 0;
	private double maxIr = -1e9, minIr = 1e9;
	private double maxRed = -1e9, minRed = 1e9;

	/* valor mostrado */
	private double spo2Smooth = -1;

	public SpO2Calculator(long windowMs, int minSamples, double alpha, double perfusionMin) {
		this.windowMs = windowMs;
		this.minSamples = minSamples;
		this.alpha = alpha;
		this.perfusionMin = perfusionMin;
	}

	/** Devuelve SpO₂ suavizado, o valor negativo si aún no hay datos fiables. */
	public double add(double ir, double red, long ts) {

		push(irQ, ir, ts, true);
		push(redQ, red, ts, false);

		purgeOld(irQ, ts, true);
		purgeOld(redQ, ts, false);

		if (irQ.size() < minSamples || redQ.size() < minSamples)
			return -1; // aún llenando ventana

		/* --- DC y AC --- */
		double dcIr = sumIr / irQ.size();
		double dcRed = sumRed / redQ.size();
		double acIr = maxIr - minIr;
		double acRed = maxRed - minRed;

		double perf = acIr / dcIr;
		if (dcIr == 0 || dcRed == 0 || perf < perfusionMin)
			return spo2Smooth; // señal débil → mantener último

		/* --- SpO₂ crudo --- */
		double ratio = (acRed / dcRed) / perf;
		double spo2 = A - B * ratio;
		spo2 = Math.max(0, Math.min(100, spo2));

		/* --- suavizado EMA --- */
		spo2Smooth = (spo2Smooth < 0) ? spo2 : alpha * spo2 + (1 - alpha) * spo2Smooth;

		return spo2Smooth;
	}

	/* ================= helpers internos ================= */
	private void push(ArrayDeque<S> q, double v, long t, boolean ir) {
		q.addLast(new S(v, t));
		if (ir) {
			sumIr += v;
			if (v > maxIr)
				maxIr = v;
			if (v < minIr)
				minIr = v;
		} else {
			sumRed += v;
			if (v > maxRed)
				maxRed = v;
			if (v < minRed)
				minRed = v;
		}
	}

	private void purgeOld(ArrayDeque<S> q, long now, boolean ir) {
		boolean recalc = false;
		while (!q.isEmpty() && now - q.peekFirst().t > windowMs) {
			double v = q.pollFirst().v;
			if (ir) {
				sumIr -= v;
				if (v == maxIr || v == minIr)
					recalc = true;
			} else {
				sumRed -= v;
				if (v == maxRed || v == minRed)
					recalc = true;
			}
		}
		if (recalc) {
			if (ir) {
				maxIr = q.stream().mapToDouble(s -> s.v).max().orElse(-1e9);
				minIr = q.stream().mapToDouble(s -> s.v).min().orElse(1e9);
			} else {
				maxRed = q.stream().mapToDouble(s -> s.v).max().orElse(-1e9);
				minRed = q.stream().mapToDouble(s -> s.v).min().orElse(1e9);
			}
		}
	}
}
