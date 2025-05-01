package org.axolotlj.RemoteHealth.app.ui;

import javafx.scene.control.TextField;

public class Utilites {
	public static void updateTextFieldColor(TextField textField, double value, double min, double max, String arguments) {
		double normalized = Math.max(0, Math.min(1, (value - min) / (max - min)));
		int r, g;

		if (normalized < 0.5) {
			r = (int) (normalized * 2 * 255);
			g = 255;
		} else {
			r = 255;
			g = (int) ((1 - normalized) * 2 * 255);
		}

		String color = String.format("#%02x%02x00", r, g);
		textField.setStyle("-fx-background-color: black;" + "-fx-text-fill: " + color + ";" + "-fx-font-weight: bold;"
				+ "-fx-border-color: black;" + "-fx-border-width: 1;" + "-fx-opacity: 1;");
	}
}
