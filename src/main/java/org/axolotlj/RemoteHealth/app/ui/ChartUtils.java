package org.axolotlj.RemoteHealth.app.ui;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.MutablePair;
import org.axolotlj.RemoteHealth.analysis.utis.FrequencyDomainAnalyzer;
import org.axolotlj.RemoteHealth.filters.Misc;
import org.axolotlj.RemoteHealth.sensor.TuplaUtil;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class ChartUtils {
	
	public static void setStyle(LineChart<Number, Number> chart, String stylePath) {
	    try {
	        String css = ChartUtils.class.getResource(stylePath).toExternalForm();
	        chart.getStylesheets().add(css);
	    } catch (Exception e) {
	        System.err.println("No se pudo cargar el stylesheet: " + stylePath);
	        e.printStackTrace();
	    }
	}

	public static void configureXAxis(LineChart<Number, Number> chart, double upperBound, double lowerBound, Double tickUnit, boolean subtractOneFromUpper) {
	    NumberAxis xAxis = (NumberAxis) chart.getXAxis();
	    xAxis.setAutoRanging(false);
	    xAxis.setLowerBound(lowerBound);
	    xAxis.setUpperBound(subtractOneFromUpper ? upperBound - 1 : upperBound);
	    if (tickUnit != null) {
	        xAxis.setTickUnit(tickUnit);
	    }
	}
	
    /** Construye una serie desde pares (timestamp, valor) para dominio del tiempo. */
    public static XYChart.Series<Number, Number> buildSeries(ArrayList<MutablePair<Long, Double>> pairs, String name) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        if (pairs == null || pairs.isEmpty()) return series;

        long t0 = pairs.get(0).getLeft();
        for (MutablePair<Long, Double> p : pairs) {
            double x = (p.getLeft() - t0) / 1000.0;
            series.getData().add(new XYChart.Data<>(x, p.getRight()));
        }
        return series;
    }

    /** Construye una serie desde la FFT (frecuencia vs magnitud). */
    public static XYChart.Series<Number, Number> buildFFTSeries(ArrayList<MutablePair<Long, Double>> pairs, String name) {
        if (pairs == null || pairs.isEmpty()) return new XYChart.Series<>();

        double[] signal = TuplaUtil.extractValues(pairs);
        long[] timestamps = TuplaUtil.extractTimestamps(pairs);
        double fs = Misc.calculateAverageSamplingRate(timestamps);

        var fft = FrequencyDomainAnalyzer.computeFFTv2(signal, fs);
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (var p : fft) {
            series.getData().add(new XYChart.Data<>(p.getLeft(), p.getRight()));
        }
        return series;
    }

    /** Limpia el chart y agrega una serie (thread-safe). */
    public static void clearAndAddSeries(LineChart<Number, Number> chart, XYChart.Series<Number, Number> series, Double upperX) {
        Platform.runLater(() -> {
            chart.getData().clear();
            chart.getData().add(series);

            // Ajustamos eje X si upperX está definido
            if (upperX != null) {
                configureXAxis(chart, upperX, 0, null, false);
            }
        });
    }

    /** Grafica en dominio del tiempo de forma directa. */
    public static void plotTimeSeries(LineChart<Number, Number> chart, ArrayList<MutablePair<Long, Double>> pairs, String seriesName) {
        XYChart.Series<Number, Number> series = buildSeries(pairs, seriesName);
        if (pairs != null && !pairs.isEmpty()) {
            long t0 = pairs.get(0).getLeft();
            long tN = pairs.get(pairs.size() - 1).getLeft();
            double durationSec = (tN - t0) / 1000.0;
            clearAndAddSeries(chart, series, durationSec);
        } else {
            clearAndAddSeries(chart, series, null);
        }
    }

    /** Grafica la FFT directamente. */
    public static void plotFrequencySeries(LineChart<Number, Number> chart, ArrayList<MutablePair<Long, Double>> pairs, String seriesName) {
        if (pairs == null || pairs.isEmpty()) return;

        long[] timestamps = TuplaUtil.extractTimestamps(pairs);
        double fs = Misc.calculateAverageSamplingRate(timestamps);
        XYChart.Series<Number, Number> fftSeries = buildFFTSeries(pairs, seriesName);

        clearAndAddSeries(chart, fftSeries, fs / 2);
    }

}
