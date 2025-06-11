package org.axolotlj.RemoteHealth.app.ui;

import static org.axolotlj.RemoteHealth.app.FxThreadUtils.runOnUIThread;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class ButtonUtils {
	
    public static void enabledButton(Button button) {
        runOnUIThread(() -> {
            button.setText("Disponible");
            button.setDisable(false);
            button.setStyle("-fx-text-fill: green;");
        });
    }

    public static void disableButton(Button button) {
        runOnUIThread(() -> {
            button.setText("Sin conexión");
            button.setDisable(true);
            button.setStyle("-fx-text-fill: red;");
        });
    }

    public static void waitingButton(Button button) {
        runOnUIThread(() -> {
            button.setText("Comprobando...");
            button.setDisable(true);
            button.setStyle("-fx-text-fill: orange;");
        });
    }

    public static void setGraphicImage(Button button, ImageView imageView) {
        runOnUIThread(() -> button.setGraphic(imageView));
    }
}
