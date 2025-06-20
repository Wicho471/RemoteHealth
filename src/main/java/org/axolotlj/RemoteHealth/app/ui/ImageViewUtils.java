package org.axolotlj.RemoteHealth.app.ui;

import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.service.logger.Log;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Utilidades para establecer imágenes en componentes ImageView en JavaFX.
 */
public class ImageViewUtils {
	private static DataLogger logger = Log.get();

    /**
     * Establece una imagen ya cargada a un ImageView con dimensiones específicas.
     *
     * @param imageView componente donde se mostrará la imagen
     * @param image imagen previamente cargada
     * @param width ancho de la imagen
     * @param height alto de la imagen
     */
    public static void setImage(ImageView imageView, Image image, int width, int height) {
        if (Platform.isFxApplicationThread()) {
            try {
                imageView.setFitWidth(width);
                imageView.setFitHeight(height);
                imageView.setImage(image);
            } catch (Exception e) {
                logger.logError("Error al asignar imagen con tamaño (" + width + "x" + height + "): " + e.getMessage());
            }
        } else {
            Platform.runLater(() -> setImage(imageView, image, width, height));
        }
    }
    
    
    /**
     * Establece una imagen desde un recurso a un ImageView.
     *
     * @param imageView componente donde se mostrará la imagen
     * @param path ruta del recurso de la imagen
     */
    public static void setImage(ImageView imageView, Image image) {
        if (Platform.isFxApplicationThread()) {
            try {
                imageView.setImage(image);
            } catch (Exception e) {
                logger.logError("Error al asignar imagen al ImageView: " + e.getMessage());
            }
        } else {
            Platform.runLater(() -> setImage(imageView, image));
        }
    }

}
