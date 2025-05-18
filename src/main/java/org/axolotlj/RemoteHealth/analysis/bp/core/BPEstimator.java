package org.axolotlj.RemoteHealth.analysis.bp.core;

import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.axolotlj.RemoteHealth.analysis.bp.util.SignalUtils;

/**
 * Estima SBP/DBP por latido a partir de eventos R y foot.
 */
class BPEstimator {

	private static final int MEAN_SIZE = 32;
	
	private static final int PTT_MIN_MS = 90;
	private static final int PTT_MAX_MS = 350;
	private static final double ALPHA_HR = 0.20;
	private static final double ALPHA_PTT = 0.20;

	private final Deque<Long> rPeaks = new ArrayDeque<>();
	private final Deque<Long> ppgFoots = new ArrayDeque<>();
	private final Deque<Double> sbpWindow = new ArrayDeque<>();
	private final Deque<Double> dbpWindow = new ArrayDeque<>();
	private Consumer<ImmutableTriple<Long ,Double, Double>> listener;

	private double a = 49, b = 10_000, c = 0.30;
	private double d = 16, e = 10_000, f = 0.20;

	private long previousRPeak = -1;
	private double ewmaHR = 0, ewmaPTT = 0;

	BPEstimator(Consumer<ImmutableTriple<Long ,Double, Double>> listener) {
		this.listener = listener;
	}

	void setCoefficients(double a, double b, double c, double d, double e, double f) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
	}

	void onRPeak(long timestamp) {
		rPeaks.add(timestamp);
		tryEmit();
	}

	void onPPGFoot(long timestamp) {
		ppgFoots.add(timestamp);
		tryEmit();
	}

	private void tryEmit() {
		while (!rPeaks.isEmpty() && !ppgFoots.isEmpty()) {
			long r = rPeaks.peekFirst();
			long foot = ppgFoots.peekFirst();

			if (foot <= r) {
				ppgFoots.pollFirst();
				continue;
			}

			long ptt = foot - r;
			if (ptt < PTT_MIN_MS || ptt > PTT_MAX_MS) {
				rPeaks.pollFirst();
				continue;
			}

			long rr = (previousRPeak < 0) ? -1 : r - previousRPeak;
			if (rr > 0 && ptt > 0.6 * rr) {
				ppgFoots.pollFirst();
				continue;
			}

			rPeaks.pollFirst();
			ppgFoots.pollFirst();
			previousRPeak = r;

			double hr = (rr <= 0) ? Double.NaN : 60_000.0 / rr;
			if (!Double.isNaN(hr) && hr >= 35 && hr <= 220)
				ewmaHR = (ewmaHR == 0) ? hr : ALPHA_HR * hr + (1 - ALPHA_HR) * ewmaHR;
			else
				continue;

			ewmaPTT = (ewmaPTT == 0) ? ptt : ALPHA_PTT * ptt + (1 - ALPHA_PTT) * ewmaPTT;

			double sbp = a + b / ewmaPTT + c * ewmaHR;
			double dbp = d + e / ewmaPTT + f * ewmaHR;

			sbpWindow.add(sbp);
			dbpWindow.add(dbp);
			if (sbpWindow.size() > MEAN_SIZE)
				sbpWindow.poll();
			if (dbpWindow.size() > MEAN_SIZE)
				dbpWindow.poll();

			listener.accept(ImmutableTriple.of(r,SignalUtils.mean(sbpWindow), SignalUtils.mean(dbpWindow)));
		}
	}
	
	void stop() {
	    rPeaks.clear();
	    ppgFoots.clear();
	    sbpWindow.clear();
	    dbpWindow.clear();
	    listener = pair -> {};
	    listener = null;
	    previousRPeak = -1;
	    ewmaHR = 0;
	    ewmaPTT = 0;
	}

}
