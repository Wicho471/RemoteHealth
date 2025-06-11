package org.axolotlj.RemoteHealth.app.ui;

import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.service.logger.Log;

import javafx.fxml.FXMLLoader;

public class FxmlUtils {
	private static DataLogger logger = Log.get();

	
	private FxmlUtils() { } 
	
	public static FXMLLoader loadFXML(String path) {
		try {
			FXMLLoader loader = new FXMLLoader(FxmlUtils.class.getResource(path));
			return loader;
		} catch (Exception e) {
			logger.logError("Error cargando el fxml -> "+e.getMessage());
			return null;
		} 
	}
}
