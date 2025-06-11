package org.axolotlj.RemoteHealth.app.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.axolotlj.RemoteHealth.analysis.utis.FrequencyDomainAnalyzer;
import org.axolotlj.RemoteHealth.filters.Misc;
import org.axolotlj.RemoteHealth.sensor.TuplaUtil;

import javafx.scene.chart.XYChart;

public class SeriesUtils {
	public static XYChart.Series<Number, Number> buildSeries(String name, List<MutablePair<Long, Double>> data,
			long absoluteStart, long maxTime, long baseTime, int limit) {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(name);
		int count = 0;
		for (MutablePair<Long, Double> point : data) {
			if (point.getLeft() < absoluteStart)
				continue;
			if (point.getLeft() > maxTime || count >= limit)
				break;
			double relativeTime = (point.getLeft() - baseTime) / 1000.0;
			series.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
			count++;
		}
		return series;
	}

	/**
	 * Construye una serie desde pares (timestamp, valor) para dominio del tiempo.
	 */
	public static XYChart.Series<Number, Number> buildSeries(ArrayList<MutablePair<Long, Double>> pairs, String name) {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(name);
		if (pairs == null || pairs.isEmpty())
			return series;

		long t0 = pairs.get(0).getLeft();
		for (MutablePair<Long, Double> p : pairs) {
			double x = (p.getLeft() - t0) / 1000.0;
			series.getData().add(new XYChart.Data<>(x, p.getRight()));
		}
		return series;
	}

	/** Construye una serie desde la FFT (frecuencia vs magnitud). */
	public static XYChart.Series<Number, Number> buildFFTSeries(ArrayList<MutablePair<Long, Double>> pairs,
			String name) {
		if (pairs == null || pairs.isEmpty())
			return new XYChart.Series<>();

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

	/**
	 * Construye una serie a partir de triples mutables, utilizando el valor de la
	 * izquierda o del medio como eje X.
	 *
	 * @param triples      Lista de triples que contienen los datos.
	 * @param name         Nombre de la serie.
	 * @param useMiddleAsX Indica si se debe usar el valor del medio como eje X; si
	 *                     es falso, se usa el valor izquierdo.
	 * @return Serie construida con los datos proporcionados.
	 */
	public static XYChart.Series<Number, Number> buildSeriesFromTriple(
			ArrayList<MutableTriple<Long, Double, Double>> triples, String name, boolean useMiddleAsX) {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName(name);

		if (triples == null || triples.isEmpty())
			return series;

		long[] xValues = TuplaUtil.extractTimestamps(triples);

		double[] yValues = useMiddleAsX ? TuplaUtil.extractMiddleValues(triples) : TuplaUtil.extractRightValues(triples);

		for (int i = 0; i < xValues.length; i++) {
			series.getData().add(new XYChart.Data<>(xValues[i], yValues[i]));
		}

		return series;
	}

	public static void updateSeriesData(XYChart.Series<Number, Number> series, int index, double value) {
		series.getData().get(index).setYValue(value);
	}

	public static void initializeSeries(XYChart.Series<Number, Number> series, int points) {
		for (int i = 0; i < points; i++) {
			series.getData().add(new XYChart.Data<>(i, 0));
		}
	}

}
