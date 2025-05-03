package org.axolotlj.RemoteHealth.app.ui;

import javafx.fxml.FXMLLoader;

public class FxmlUtils {
	
	public static FXMLLoader loadFXML(String path) {
		try {
			FXMLLoader loader = new FXMLLoader(FxmlUtils.class.getResource(path));
			return loader;
		} catch (Exception e) {
			System.err.println("Error cargando el fxml -> "+e.getMessage());
			return null;
		} 
	}
}
