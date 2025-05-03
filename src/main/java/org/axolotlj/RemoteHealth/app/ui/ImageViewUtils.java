package org.axolotlj.RemoteHealth.app.ui;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageViewUtils {
	
	public static void setImage(ImageView imageView, String path) {
        Platform.runLater(() -> {
            try {
                Image image = new Image(ImageViewUtils.class.getResource(path).toExternalForm());
                imageView.setImage(image);
            } catch (Exception e) {
                System.err.println("No se pudo cargar la imagen: " + path);
                e.printStackTrace();
            }
        });
    }
}
