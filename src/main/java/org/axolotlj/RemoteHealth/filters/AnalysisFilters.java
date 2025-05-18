package org.axolotlj.RemoteHealth.filters;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.axolotlj.RemoteHealth.config.filt.AnalysisFiltersConfig;
import org.axolotlj.RemoteHealth.filters.core.IIRFilterring;
import org.axolotlj.RemoteHealth.filters.core.SavitzkyGolayFilter;
import org.axolotlj.RemoteHealth.filters.core.WaveletDenoiser;
import org.axolotlj.RemoteHealth.sensor.TuplaUtil;
import org.axolotlj.RemoteHealth.sensor.data.DataPoint;
import org.axolotlj.RemoteHealth.sensor.handle.DataExtractor;
import org.axolotlj.RemoteHealth.sensor.handle.SensorField;

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
    public ArrayList<MutablePair<Long, Double>> getEcg(ArrayList<DataPoint> structureDatas) {
        ArrayList<MutablePair<Long, Double>> extractedData = DataExtractor.extractValidValues(structureDatas, SensorField.ECG);
        long[] timestamps = TuplaUtil.extractTimestamps(extractedData);
        double[] values = TuplaUtil.extractValues(extractedData);

        double samplingRate = Misc.calculateAverageSamplingRate(timestamps);

        double[] valuesFiltered = filterEgc(values, samplingRate);
        TuplaUtil.assignTuplaValues(extractedData, valuesFiltered);

        return extractedData;
    }

    /**
     * Aplica el conjunto completo de filtros a la señal PPG (IR o RED).
     * 
     * @param extractedData lista de pares timestamp-valor sin procesar
     * @return lista de pares timestamp-valor filtrados
     */
    public ArrayList<MutableTriple<Long, Double, Double>> getPleth(ArrayList<DataPoint> structureDatas) {
    	
    	ArrayList<MutableTriple<Long, Double, Double>> extractedData = DataExtractor.extractValidValues(structureDatas, SensorField.IR, SensorField.RED);
    	
        long[] timestamps = TuplaUtil.extractTimestamps(extractedData);
        double[] ir = TuplaUtil.extractMiddleValues(extractedData);
        double[] red = TuplaUtil.extractRightValues(extractedData);
        
        double samplingRate = Misc.calculateAverageSamplingRate(timestamps);

        double[] irFiltered = filterPleth(ir, samplingRate);
        double[] redFiltered = filterPleth(red, samplingRate);
        
        TuplaUtil.assignTuplaValues(extractedData, irFiltered, redFiltered);

        return extractedData;
    }

    private double[] filterEgc(double[] values, double fs) {
        if (fs <= 0) throw new IllegalArgumentException("Frecuencia de muestreo inválida: " + fs);

        double[] filtered = Arrays.copyOf(values, values.length);

        
        for (int i = 0; i < filtered.length; i++) {
			filtered[i] = Misc.adcToVolts(filtered[i]);
		}

        filtered = IIRFilterring.applyButterworthFilter(
            filtered, fs,
            configFile.getEcgBandstopOrder(),
            FilterType.BANDSTOP,
            configFile.getEcgBandstopLow(),
            configFile.getEcgBandstopHigh()
        );

        filtered = IIRFilterring.applyButterworthFilter(
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

        for (int i = 0; i < filtered.length; i++) {
			filtered[i] = Misc.normalizePleth(filtered[i]);
		}
        
        filtered = IIRFilterring.applyButterworthFilter(
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
