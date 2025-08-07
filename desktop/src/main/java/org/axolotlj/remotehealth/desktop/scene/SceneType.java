package org.axolotlj.remotehealth.desktop.scene;

import org.axolotlj.remotehealth.desktop.utils.Images;
import org.axolotlj.remotehealth.desktop.utils.DesktopPaths;

import javafx.scene.image.Image;

/**
 * Tipos de escenas disponibles en la aplicación.
 */
public enum SceneType {
	DEVICE_SELECTOR("Selector de Dispositivo", DesktopPaths.VIEW_SCENE_STARTUPSCENE_FXML, Images.IMG_FAVICONS_APP_ICON),
	DEVICE_SETUP("Configuración de Dispositivo", DesktopPaths.VIEW_SCENE_DEVICESETUPSCENE_FXML, Images.IMG_FAVICONS_QR),
	DASHBOARD("Panel de Control", DesktopPaths.VIEW_SCENE_DASHBOARDSCENE_FXML, Images.IMG_FAVICONS_DASHBOARD),
	FLASH_ESP("Flashear ESP32", DesktopPaths.VIEW_SCENE_FLASHESP32SCENE_FXML, Images.IMG_FAVICONS_UPLOAD),
	ANALYSIS("Análisis de Datos", DesktopPaths.VIEW_SCENE_DATAANALYSISSCENE_FXML, Images.IMG_FAVICONS_ANALYSIS),
	FILTERS_SETTINGS("Configuración de Filtros", DesktopPaths.VIEW_SCENE_FILTERSETTINGSSCENE_FXML,
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
