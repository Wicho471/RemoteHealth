package org.axolotlj.RemoteHealth.app.ui;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

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

}
