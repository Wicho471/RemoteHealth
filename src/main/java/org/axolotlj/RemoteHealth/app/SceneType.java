package org.axolotlj.RemoteHealth.app;

import org.axolotlj.RemoteHealth.util.paths.Paths;

import javafx.scene.image.Image;

/**
 * Tipos de escenas disponibles en la aplicación.
 */
public enum SceneType {
	DEVICE_SELECTOR("Selector de Dispositivo", Paths.VIEW_SCENE_STARTUPSCENE_FXML, Images.IMG_FAVICONS_APP_ICON),
	DEVICE_SETUP("Configuración de Dispositivo", Paths.VIEW_SCENE_DEVICESETUPSCENE_FXML, Images.IMG_FAVICONS_QR),
	ESP32_TOOLS("Herramientas ESP32", Paths.VIEW_SCENE_ESP32TOOLSSCENE_FXML, Images.IMG_FAVICONS_MICROCONTROLER),
	DASHBOARD("Panel de Control", Paths.VIEW_SCENE_DASHBOARDSCENE_FXML, Images.IMG_FAVICONS_DASHBOARD),
	FLASH_ESP("Flashear ESP32", Paths.VIEW_SCENE_FLASHESP32SCENE_FXML, Images.IMG_FAVICONS_UPLOAD),
	ANALYSIS("Análisis de Datos", Paths.VIEW_SCENE_DATAANALYSISSCENE_FXML, Images.IMG_FAVICONS_ANALYSIS),
	FILTERS_SETTINGS("Configuración de Filtros", Paths.VIEW_SCENE_FILTERSETTINGSSCENE_FXML,
			Images.IMG_FAVICONS_SETTINGS);

	private final String title;
	private final String fxmlPath;
	private final Image icon;

	SceneType(String title, String fxmlPath, Image icon) {
		this.title = title;
		this.fxmlPath = fxmlPath;
		this.icon = icon;
	}

	public String getTitle() {
		return title;
	}

	public String getFxmlPath() {
		return fxmlPath;
	}

	public Image getImage() {
		return icon;
	}
}
