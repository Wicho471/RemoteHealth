package org.axolotlj.remotehealth.core.javafx.util;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;

import javafx.fxml.FXMLLoader;

public class FxmlUtils {
	private static DataLogger logger = Log.get();

	private FxmlUtils() { } 
	
	public static FXMLLoader loadFXML(String path) {
		if (path == null) {
			logger.logFatal("La ruta no puede ser nula");
			return null;
		}
		try {
			FXMLLoader loader = new FXMLLoader(FxmlUtils.class.getResource(path));
			return loader;
		} catch (Exception e) {
			logger.logException("Error cargando el fxml", e);
			return null;
		} 
	}
}
