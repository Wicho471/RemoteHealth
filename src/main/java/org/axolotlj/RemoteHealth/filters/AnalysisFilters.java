package org.axolotlj.RemoteHealth.filters;

import java.util.Arrays;

import org.axolotlj.RemoteHealth.config.files.AnalysisFiltersConfig;
import org.axolotlj.RemoteHealth.filters.ButterworthFilter.FilterType;

import jwave.exceptions.JWaveException;

public class AnalysisFilters {

    private final AnalysisFiltersConfig configFile;

    public AnalysisFilters() {
        this.configFile = new AnalysisFiltersConfig();
        this.configFile.loadProperties();
    }

    public double[] filterEgc(double[] values, double fs) {
        if (fs <= 0) throw new IllegalArgumentException("Frecuencia de muestreo inválida: " + fs);

        double[] filtered = Arrays.copyOf(values, values.length);
        long start, duration;

        start = System.nanoTime();
        filtered = Misc.normalize(filtered);
        duration = System.nanoTime() - start;
        System.out.println("ECG -> Normalization: " + duration / 1_000_000.0 + " ms");

        start = System.nanoTime();
        filtered = ButterworthFilter.applyButterworthFilter(
            filtered, fs,
            configFile.getEcgBandstopOrder(),
            FilterType.BANDSTOP,
            configFile.getEcgBandstopLow(),
            configFile.getEcgBandstopHigh()
        );
        duration = System.nanoTime() - start;
        System.out.println("ECG -> Butterworth BANDSTOP: " + duration / 1_000_000.0 + " ms");

        start = System.nanoTime();
        filtered = ButterworthFilter.applyButterworthFilter(
            filtered, fs,
            configFile.getEcgBandpassOrder(),
            FilterType.BANDPASS,
            configFile.getEcgBandpassLow(),
            configFile.getEcgBandpassHigh()
        );
        duration = System.nanoTime() - start;
        System.out.println("ECG -> Butterworth BANDPASS: " + duration / 1_000_000.0 + " ms");

        start = System.nanoTime();
        try {
            filtered = WaveletDenoiser.waveletDenoise(
                filtered,
                configFile.getEcgWaveletType(),
                configFile.getEcgWaveletLevel(),
                configFile.getEcgWaveletThreshold(),
                configFile.isEcgWaveletSoft()
            );
        } catch (JWaveException e) {
            e.printStackTrace();
        }
        duration = System.nanoTime() - start;
        System.out.println("ECG -> Wavelet Denoising: " + duration / 1_000_000.0 + " ms");

        start = System.nanoTime();
        filtered = SavitzkyGolayFilter.filter(filtered, configFile.getEcgSGWindow(), configFile.getEcgSGPoly());
        duration = System.nanoTime() - start;
        System.out.println("ECG -> Savitzky-Golay: " + duration / 1_000_000.0 + " ms");

        return filtered;
    }

    public double[] filterToPleth(double[] values, double fs) {
        if (fs <= 0) throw new IllegalArgumentException("Frecuencia de muestreo inválida: " + fs);

        double[] filtered = Arrays.copyOf(values, values.length);
        long start, duration;

        System.out.println("PLETH -> Original (5): " + Arrays.toString(Arrays.copyOf(filtered, 5)));

        start = System.nanoTime();
        filtered = ButterworthFilter.applyButterworthFilter(
            filtered, fs,
            configFile.getPlethBandpassOrder(),
            FilterType.BANDPASS,
            configFile.getPlethBandpassLow(),
            configFile.getPlethBandpassHigh()
        );
        duration = System.nanoTime() - start;
        System.out.println("PLETH -> BANDPASS: " + duration / 1_000_000.0 + " ms");

        start = System.nanoTime();
        try {
            filtered = WaveletDenoiser.waveletDenoise(
                filtered,
                configFile.getPlethWaveletType(),
                configFile.getPlethWaveletLevel(),
                configFile.getPlethWaveletThreshold(),
                configFile.isPlethWaveletSoft()
            );
        } catch (JWaveException e) {
            e.printStackTrace();
        }
        duration = System.nanoTime() - start;
        System.out.println("PLETH -> Wavelet: " + duration / 1_000_000.0 + " ms");

        start = System.nanoTime();
        filtered = SavitzkyGolayFilter.filter(filtered, configFile.getPlethSGWindow(), configFile.getPlethSGPoly());
        duration = System.nanoTime() - start;
        System.out.println("PLETH -> Savitzky-Golay: " + duration / 1_000_000.0 + " ms");

        System.out.println("PLETH -> Final (5): " + Arrays.toString(Arrays.copyOf(filtered, 5)));

        long countNaN = Arrays.stream(filtered).filter(Double::isNaN).count();
        if (countNaN > 0) {
            System.err.println("🚨 Se detectaron " + countNaN + " valores NaN en Pleth");
        }

        return filtered;
    }
}
