package org.axolotlj.RemoteHealth.app.ui;

import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;

import javafx.scene.chart.XYChart;

public class SeriesUtils {
	public static XYChart.Series<Number, Number> buildSeries(String name, List<MutablePair<Long, Double>> data, long absoluteStart, long maxTime, long baseTime, int limit) {
	    XYChart.Series<Number, Number> series = new XYChart.Series<>();
	    series.setName(name);
	    int count = 0;
	    for (MutablePair<Long, Double> point : data) {
	        if (point.getLeft() < absoluteStart) continue;
	        if (point.getLeft() > maxTime || count >= limit) break;
	        double relativeTime = (point.getLeft() - baseTime) / 1000.0;
	        series.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
	        count++;
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
