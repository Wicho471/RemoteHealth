package org.axolotlj.remotehealth.desktop.utils;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.path.SharedPaths;

import javafx.scene.text.Font;


public class Fonts {
	private static DataLogger logger = Log.get();
	
	/**
	 * Carga una fuente desde el recurso especificado.
	 *
	 * @param resourcePath Ruta relativa al archivo de fuente (.ttf), por ejemplo: "/fonts/Roboto-Regular.ttf"
	 * @param size Tamaño deseado de la fuente.
	 * @return Instancia de {@link Font} cargada, o una fuente por defecto si no se pudo cargar.
	 */
	public static Font load(String resourcePath, double size) {
	    if (resourcePath == null) {
	        logger.logFatal("La ruta del recurso de fuente es null.");
	        throw new IllegalArgumentException("La ruta del recurso no puede ser null.");
	    }

	    try (var stream = Fonts.class.getResourceAsStream(resourcePath)) {
	        if (stream == null) {
	            logger.logWarn("No se encontró el recurso de fuente: " + resourcePath);
	            return Font.font("Monospaced", size);
	        }

	        Font font = Font.loadFont(stream, size);
	        if (font == null) {
	            logger.logWarn("No se pudo cargar la fuente desde el stream: " + resourcePath);
	            return Font.font("Monospaced", size);
	        }

	        logger.logDebug("Fuente cargada con éxito: " + resourcePath);
	        return font;

	    } catch (Exception e) {
	        logger.logException("Excepción al cargar la fuente '" + resourcePath + "': ", e);
	        return Font.font("Monospaced", size);
	    }
	}

    
    public static Font UBUNTU_MONO_REGULAR = load(SharedPaths.FONTS_UBUNTU_REGULAR_PATH, 14);
}
