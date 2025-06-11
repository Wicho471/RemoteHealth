package org.axolotlj.RemoteHealth.app.ui;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.MutablePair;
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

	public static void configureXAxis(LineChart<Number, Number> chart, double upperBound, double lowerBound,
			Double tickUnit, boolean subtractOneFromUpper) {
		NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(lowerBound);
		xAxis.setUpperBound(subtractOneFromUpper ? upperBound - 1 : upperBound);
		if (tickUnit != null) {
			xAxis.setTickUnit(tickUnit);
		}
	}

	/** Limpia el chart y agrega una serie (thread-safe). */
	public static void clearAndAddSeries(LineChart<Number, Number> chart, XYChart.Series<Number, Number> series,
			Double upperX) {
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
	public static void plotTimeSeries(LineChart<Number, Number> chart, ArrayList<MutablePair<Long, Double>> pairs,
			String seriesName) {
		XYChart.Series<Number, Number> series = SeriesUtils.buildSeries(pairs, seriesName);
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
	public static void plotFrequencySeries(LineChart<Number, Number> chart, ArrayList<MutablePair<Long, Double>> pairs,
			String seriesName) {
		if (pairs == null || pairs.isEmpty())
			return;

		long[] timestamps = TuplaUtil.extractTimestamps(pairs);
		double fs = Misc.calculateAverageSamplingRate(timestamps);
		XYChart.Series<Number, Number> fftSeries = SeriesUtils.buildFFTSeries(pairs, seriesName);

		clearAndAddSeries(chart, fftSeries, fs / 2);
	}

	/**
	 * Grafica dos series de frecuencia (FFT) en el mismo LineChart.
	 *
	 * @param chart  LineChart en el que se graficarán las FFTs.
	 * @param pairs1 Primer conjunto de datos en el tiempo.
	 * @param pairs2 Segundo conjunto de datos en el tiempo.
	 * @param name1  Nombre de la primera serie.
	 * @param name2  Nombre de la segunda serie.
	 */
	@SuppressWarnings("unchecked")
	public static void plotTwoFrequencySeriesFromPairs(LineChart<Number, Number> chart,
	        ArrayList<MutablePair<Long, Double>> pairs1,
	        ArrayList<MutablePair<Long, Double>> pairs2,
	        String name1,
	        String name2) {

	    if (chart == null || pairs1 == null || pairs2 == null || name1 == null || name2 == null) {
	        throw new IllegalArgumentException("Ningún parámetro puede ser nulo.");
	    }

	    Platform.runLater(() -> {
	        chart.getData().clear();

	        double fs1 = Misc.calculateAverageSamplingRate(TuplaUtil.extractTimestamps(pairs1));
	        double fs2 = Misc.calculateAverageSamplingRate(TuplaUtil.extractTimestamps(pairs2));

	        XYChart.Series<Number, Number> fft1 = SeriesUtils.buildFFTSeries(pairs1, name1);
	        XYChart.Series<Number, Number> fft2 = SeriesUtils.buildFFTSeries(pairs2, name2);

	        chart.getData().addAll(fft1, fft2);

	        double upperBound = Math.max(fs1, fs2) / 2.0;
	        configureXAxis(chart, upperBound, 0, null, false);
	    });
	}
	
	/**
	 * Grafica dos series de tiempo basadas en pares (timestamp, valor) en el mismo
	 * LineChart.
	 *
	 * @param chart   LineChart en el que se graficarán las series.
	 * @param series1 Primera serie de datos.
	 * @param series2 Segunda serie de datos.
	 * @param name1   Nombre de la primera serie.
	 * @param name2   Nombre de la segunda serie.
	 */
	@SuppressWarnings("unchecked")
	public static void plotTwoTimeSeriesFromPairs(LineChart<Number, Number> chart,
			ArrayList<MutablePair<Long, Double>> series1, ArrayList<MutablePair<Long, Double>> series2, String name1,
			String name2) {
		if (chart == null || series1 == null || series2 == null || name1 == null || name2 == null) {
			throw new IllegalArgumentException("Ningún parámetro puede ser nulo.");
		}

		Platform.runLater(() -> {
			chart.getData().clear();

			XYChart.Series<Number, Number> s1 = SeriesUtils.buildSeries(series1, name1);
			XYChart.Series<Number, Number> s2 = SeriesUtils.buildSeries(series2, name2);

			chart.getData().addAll(s1, s2);

			double duration1 = (series1.get(series1.size() - 1).getLeft() - series1.get(0).getLeft()) / 1000.0;
			double duration2 = (series2.get(series2.size() - 1).getLeft() - series2.get(0).getLeft()) / 1000.0;

			double maxDuration = Math.max(duration1, duration2);

			if (maxDuration > 0) {
				configureXAxis(chart, maxDuration, 0, null, false);
			}
		});
	}
}
