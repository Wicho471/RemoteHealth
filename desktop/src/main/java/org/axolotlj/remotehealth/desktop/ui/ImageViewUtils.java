package org.axolotlj.remotehealth.desktop.ui;

import static org.axolotlj.remotehealth.desktop.javafx.current.FxThreadUtils.runOnUIThread;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;

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
	 * @param image     imagen previamente cargada
	 * @param width     ancho de la imagen
	 * @param height    alto de la imagen
	 */
	public static void setImage(ImageView imageView, Image image, int width, int height) {
		runOnUIThread(() -> {
			try {
				imageView.setFitWidth(width);
				imageView.setFitHeight(height);
				imageView.setImage(image);
			} catch (Exception e) {
				logger.logException("Error al asignar imagen con tamaño (" + width + "x" + height + ")", e);
			}
		});
	}

	/**
	 * Establece una imagen desde un recurso a un ImageView.
	 *
	 * @param imageView componente donde se mostrará la imagen
	 * @param path      ruta del recurso de la imagen
	 */
	public static void setImage(ImageView imageView, Image image) {
        runOnUIThread(() -> {
			try {
				imageView.setImage(image);
			} catch (Exception e) {
				logger.logException("Error al asignar imagen al ImageView: ", e);
			}
        });
	}
}
