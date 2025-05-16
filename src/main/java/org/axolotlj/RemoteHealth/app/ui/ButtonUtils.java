package org.axolotlj.RemoteHealth.app.ui;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class ButtonUtils {
	public static void enabledButton(Button button) {
		if (Platform.isFxApplicationThread()) {
			button.setText("Disponible");
			button.setDisable(false);
			button.setStyle("-fx-text-fill: green;");
		} else {
			Platform.runLater(() -> enabledButton(button));
		}
	}

	public static void disableButton(Button button) {
		if (Platform.isFxApplicationThread()) {
			button.setText("Sin conexión");
			button.setDisable(true);
			button.setStyle("-fx-text-fill: red;");
		} else {
			Platform.runLater(() -> disableButton(button));
		}
	}

	public static void waitingButton(Button button) {
		if (Platform.isFxApplicationThread()) {
			button.setText("Comprobando...");
			button.setDisable(true);
			button.setStyle("-fx-text-fill: orange;");
		} else {
			Platform.runLater(() -> waitingButton(button));
		}
	}

	public static void setGraphicImage(Button button, ImageView imageView) {
	    if (Platform.isFxApplicationThread()) {
	        button.setGraphic(imageView);
	    } else {
	        Platform.runLater(() -> setGraphicImage(button, imageView));
	    }
	}

}
