package org.axolotlj.RemoteHealth.filters;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.MutablePair;
import org.axolotlj.RemoteHealth.config.files.AnalysisFiltersConfig;
import org.axolotlj.RemoteHealth.filters.ButterworthFilter.FilterType;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.util.DataHandler;
import org.axolotlj.RemoteHealth.util.MutablePairHandler;

import jwave.exceptions.JWaveException;

/**
 * Clase responsable de aplicar filtros de análisis a señales fisiológicas como ECG, IR y RED.
 */
public class AnalysisFilters {

    private final AnalysisFiltersConfig configFile;

    public AnalysisFilters() {
        this.configFile = new AnalysisFiltersConfig();
        this.configFile.loadProperties();
    }

    /**
     * Aplica el conjunto completo de filtros a la señal ECG.
     * 
     * @param structureDatas lista de datos sin procesar
     * @return lista de pares timestamp-valor filtrados
     */
    public ArrayList<MutablePair<Long, Double>> applyFiltersToEcg(ArrayList<StructureData> structureDatas) {
        ArrayList<MutablePair<Long, Double>> extractedData = DataHandler.extractValidPairs(structureDatas, DataHandler.SensorField.ECG);
        long[] timestamps = MutablePairHandler.extractTimestamps(extractedData);
        double[] values = MutablePairHandler.extractValues(extractedData);

        double samplingRate = Misc.calculateAverageSamplingRate(timestamps);

        double[] valuesFiltered = filterEgc(values, samplingRate);
        MutablePairHandler.assignValuesToPairs(extractedData, valuesFiltered);

        return extractedData;
    }

    /**
     * Aplica el conjunto completo de filtros a la señal PPG (IR o RED).
     * 
     * @param extractedData lista de pares timestamp-valor sin procesar
     * @return lista de pares timestamp-valor filtrados
     */
    public ArrayList<MutablePair<Long, Double>> applyFiltersToPleth(ArrayList<MutablePair<Long, Double>> extractedData) {
        long[] timestamps = MutablePairHandler.extractTimestamps(extractedData);
        double[] values = MutablePairHandler.extractValues(extractedData);

        double samplingRate = Misc.calculateAverageSamplingRate(timestamps);

        double[] valuesFiltered = filterPleth(values, samplingRate);
        MutablePairHandler.assignValuesToPairs(extractedData, valuesFiltered);

        return extractedData;
    }

    private double[] filterEgc(double[] values, double fs) {
        if (fs <= 0) throw new IllegalArgumentException("Frecuencia de muestreo inválida: " + fs);

        double[] filtered = Arrays.copyOf(values, values.length);

        filtered = Misc.normalize(filtered);

        filtered = ButterworthFilter.applyButterworthFilter(
            filtered, fs,
            configFile.getEcgBandstopOrder(),
            FilterType.BANDSTOP,
            configFile.getEcgBandstopLow(),
            configFile.getEcgBandstopHigh()
        );

        filtered = ButterworthFilter.applyButterworthFilter(
            filtered, fs,
            configFile.getEcgBandpassOrder(),
            FilterType.BANDPASS,
            configFile.getEcgBandpassLow(),
            configFile.getEcgBandpassHigh()
        );

        try {
            filtered = WaveletDenoiser.waveletDenoise(
                filtered,
                configFile.getEcgWaveletType(),
                configFile.getEcgWaveletLevel(),
                configFile.getEcgWaveletThreshold(),
                configFile.isEcgWaveletSoft()
            );
        } catch (JWaveException e) {
            System.err.println("ECG -> Excepción en waveletDenoise: " + e.getMessage());
        }

        filtered = SavitzkyGolayFilter.filter(filtered, configFile.getEcgSGWindow(), configFile.getEcgSGPoly());

        return filtered;
    }

    private double[] filterPleth(double[] values, double fs) {
        if (fs <= 0) throw new IllegalArgumentException("Frecuencia de muestreo inválida: " + fs);

        double[] filtered = Arrays.copyOf(values, values.length);

        filtered = ButterworthFilter.applyButterworthFilter(
            filtered, fs,
            configFile.getPlethBandpassOrder(),
            FilterType.BANDPASS,
            configFile.getPlethBandpassLow(),
            configFile.getPlethBandpassHigh()
        );

        try {
            filtered = WaveletDenoiser.waveletDenoise(
                filtered,
                configFile.getPlethWaveletType(),
                configFile.getPlethWaveletLevel(),
                configFile.getPlethWaveletThreshold(),
                configFile.isPlethWaveletSoft()
            );
        } catch (JWaveException e) {
            System.err.println("Pleth -> Excepción en waveletDenoise: " + e.getMessage());
        }

        filtered = SavitzkyGolayFilter.filter(filtered, configFile.getPlethSGWindow(), configFile.getPlethSGPoly());

        long countNaN = Arrays.stream(filtered).filter(Double::isNaN).count();
        if (countNaN > 0) {
            System.err.println("🚨 Se detectaron " + countNaN + " valores NaN en Pleth");
        }

        return filtered;
    }
}
