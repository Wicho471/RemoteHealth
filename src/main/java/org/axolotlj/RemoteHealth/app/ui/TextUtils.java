package org.axolotlj.RemoteHealth.app.ui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class TextUtils {
	public static void updateTextFieldColor(TextField textField, double value, double min, double max) {
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
	
    public static void setText(TextField field, String value) {
        Platform.runLater(() -> field.setText(value));
    }

    public static void setText(TextField field, int value) {
        Platform.runLater(() -> field.setText(Integer.toString(value)));
    }

    public static void setText(TextField field, double value) {
        Platform.runLater(() -> field.setText(String.format("%.2f", value)));
    }

    public static void setText(TextField field, float value) {
        Platform.runLater(() -> field.setText(String.format("%.2f", value)));
    }
    
    public static void setText(TextField field, boolean value) {
        Platform.runLater(() -> field.setText(Boolean.toString(value)));
    }

    public static void setText(TextField field, Object value) {
        Platform.runLater(() -> field.setText(value != null ? value.toString() : ""));
    }
    
    public static void setText(TextArea area, String value) {
        Platform.runLater(() -> area.setText(value));
    }

    public static void setText(TextArea area, int value) {
        Platform.runLater(() -> area.setText(Integer.toString(value)));
    }

    public static void setText(TextArea area, double value) {
        Platform.runLater(() -> area.setText(String.format("%.2f", value)));
    }

    public static void setText(TextArea area, float value) {
        Platform.runLater(() -> area.setText(String.format("%.2f", value)));
    }

    public static void setText(TextArea area, boolean value) {
        Platform.runLater(() -> area.setText(Boolean.toString(value)));
    }

    public static void setText(TextArea area, Object value) {
        Platform.runLater(() -> area.setText(value != null ? value.toString() : ""));
    }
}
