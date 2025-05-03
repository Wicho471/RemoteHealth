package org.axolotlj.RemoteHealth.app.ui;

import javafx.application.Platform;
import javafx.scene.control.Button;

public class ButtonUtils {
	public static void enabledButton(Button button) {
		Platform.runLater(() -> {
			button.setText("Disponible");
			button.setDisable(false);
			button.setStyle("-fx-text-fill: green;");
		});
	}

	public static void disableButton(Button button) {
		Platform.runLater(() -> {
			button.setText("Sin conexión");
			button.setDisable(true);
			button.setStyle("-fx-text-fill: red;");
		});
	}
	
	public static void waitingButton(Button button) {
		button.setText("Comprobando...");
		button.setDisable(true);
		button.setStyle("-fx-text-fill: orange;");
	}
}
