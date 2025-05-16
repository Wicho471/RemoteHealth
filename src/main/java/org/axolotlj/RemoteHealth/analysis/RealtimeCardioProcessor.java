package org.axolotlj.RemoteHealth.analysis;

import com.github.psambit9791.jdsp.signal.peaks.FindPeak;
import com.github.psambit9791.jdsp.signal.peaks.Peak;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.util.*;

/**
 * Procesa muestras *YA FILTRADAS* de ECG y PPG para obtener BPM y SpO₂.
 *
 *  ▸ addEcgSample(filteredEcg, tsMs)   → OptionalDouble BPM
 *  ▸ addPlethSample(ir, red, tsMs)     → OptionalDouble SpO₂
 */
public class RealtimeCardioProcessor {

    /* ------------------------- Config ------------------------- */
    public static final class Config {
        public final double fs;                 // Hz
        public final int    beatsInWindow;      // p. ej. 5
        public final double spo2WindowSec;      // p. ej. 6 s
        public final double spo2A, spo2B;       // coef. lineal SpO₂ = A – B·R

        public Config(double fs, int beatsInWindow,
                      double spo2WindowSec, double spo2A, double spo2B) {
            this.fs = fs;
            this.beatsInWindow = beatsInWindow;
            this.spo2WindowSec = spo2WindowSec;
            this.spo2A = spo2A;
            this.spo2B = spo2B;
        }
    }

    private final Config cfg;

    /* ----------- buffers BPM ----------- */
    private final Deque<Double> integBuf = new ArrayDeque<>();
    private final Deque<Long>   rPeakTimes = new ArrayDeque<>();
    private double prevSample = 0;
    private double integSum   = 0;
    private final int integWin;                // ≈150 ms → 0.15*fs

    private double sPk = 0, nPk = 0, thr = 0;

    /* ----------- buffers SpO₂ ----------- */
    private final Deque<Double> redBuf  = new ArrayDeque<>();
    private final Deque<Double> irBuf   = new ArrayDeque<>();
    private final Deque<Long>   timeBuf = new ArrayDeque<>();

    public RealtimeCardioProcessor(Config cfg) {
        this.cfg = cfg;
        this.integWin = (int) Math.round(0.15 * cfg.fs);
    }

    /* ==========================================================
     * ECG ► BPM
     * =========================================================*/
    public OptionalDouble addEcgSample(double ecg, long tMs) {

        /* 1. Derivada y al cuadrado */
        double der = ecg - prevSample;
        prevSample = ecg;
        double energy = der * der;

        /* 2. Integración móvil 150 ms ----------------------- */
        integBuf.addLast(energy);
        integSum += energy;
        if (integBuf.size() > integWin)
            integSum -= integBuf.removeFirst();
        double integ = integSum / integBuf.size();

        /* 3. Detección de QRS con umbral adaptativo --------- */
        boolean isPeak = integ > thr;
        updateThresholds(integ, isPeak);

        /* Search-back: si pasaron >1.66·RR_med y nada, baja el umbral a la mitad */
        if (!isPeak && !rPeakTimes.isEmpty()) {
            long last = rPeakTimes.peekLast();
            long delta = tMs - last;
            double estRR = estimateMedianRR();
            if (estRR > 0 && delta > 1.66 * estRR)
                thr *= 0.5;
        }

        if (isPeak && isNewBeat(tMs)) {
            rPeakTimes.addLast(tMs);
            if (rPeakTimes.size() > cfg.beatsInWindow + 1)
                rPeakTimes.removeFirst();
        }

        /* 4. Salida BPM ------------------------------------- */
        if (rPeakTimes.size() < 2) return OptionalDouble.empty();
        double medRR = estimateMedianRR();
        double bpm = 60_000.0 / medRR;
        return OptionalDouble.of(bpm);
    }

    private boolean isNewBeat(long tMs) {
        if (rPeakTimes.isEmpty()) return true;
        long rr = tMs - rPeakTimes.peekLast();
        return rr > 0.3 * 1000;               // >300 ms (≤200 bpm)
    }

    private double estimateMedianRR() {
        int k = rPeakTimes.size();
        if (k < 2) return -1;
        double[] rr = new double[k - 1];
        Long[] ts = rPeakTimes.toArray(new Long[0]);
        for (int i = 1; i < k; i++) rr[i - 1] = ts[i] - ts[i - 1];
        return new Median().evaluate(rr);
    }

    private void updateThresholds(double integ, boolean isPeak) {
        if (isPeak) sPk = 0.125 * integ + 0.875 * sPk;
        else        nPk = 0.125 * integ + 0.875 * nPk;
        thr = nPk + 0.25 * (sPk - nPk);
    }

    /* ==========================================================
     * PPG ► SpO₂   (IR + Red)
     * =========================================================*/
    public OptionalDouble addPlethSample(double ir, double red, long tMs) {

        irBuf.addLast(ir);
        redBuf.addLast(red);
        timeBuf.addLast(tMs);

        purgeOld(tMs);

        if (timeBuf.size() < cfg.fs)  // mínimo 1 s
            return OptionalDouble.empty();

        /* Detectar picos en ventana */
        double[] irArr  = irBuf .stream().mapToDouble(Double::doubleValue).toArray();
        double[] redArr = redBuf.stream().mapToDouble(Double::doubleValue).toArray();

        Peak irPeaks  = new FindPeak(irArr ).detectPeaks();
        Peak redPeaks = new FindPeak(redArr).detectPeaks();

        if (irPeaks.getPeaks().length < 2 || redPeaks.getPeaks().length < 2)
            return OptionalDouble.empty();

        double acIr   = peakToPeak(irArr , irPeaks .getPeaks());
        double dcIr   = mean(irArr);
        double acRed  = peakToPeak(redArr, redPeaks.getPeaks());
        double dcRed  = mean(redArr);

        if (dcIr == 0 || dcRed == 0) return OptionalDouble.empty();

        double R = (acRed / dcRed) / (acIr / dcIr);
        double spo2 = cfg.spo2A - cfg.spo2B * R;
        return OptionalDouble.of(Math.max(0, Math.min(100, spo2)));
    }

    private void purgeOld(long now) {
        long winMs = (long) (cfg.spo2WindowSec * 1000);
        while (!timeBuf.isEmpty() && now - timeBuf.peekFirst() > winMs) {
            timeBuf.removeFirst();
            irBuf .removeFirst();
            redBuf.removeFirst();
        }
    }

    private static double peakToPeak(double[] d, int[] idx) {
        double max = Arrays.stream(idx).mapToDouble(i -> d[i]).max().orElse(0);
        double min = Arrays.stream(idx).mapToDouble(i -> d[i]).min().orElse(0);
        return max - min;
    }
    private static double mean(double[] a) { return Arrays.stream(a).average().orElse(0); }
}
