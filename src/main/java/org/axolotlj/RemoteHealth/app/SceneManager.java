package org.axolotlj.RemoteHealth.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import org.axolotlj.RemoteHealth.app.ui.FxmlUtils;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.util.Paths;

/**
 * Administrador de escenas de la aplicación.
 */
public class SceneManager {

	public enum SceneType {
		DEVICE_SELECTOR("Selector de Dispositivo", Paths.FXML_STARTUP_SCENE, Paths.IMG_APP_ICON),
		DEVICE_SETUP("Configuración de Dispositivo", Paths.FXML_DEVICE_SETUP_SCENE, Paths.QR_ICON),
		ESP32_TOOLS("Herramientas ESP32", Paths.FXML_ESP32_TOOLS_SCENE, Paths.ESP32_ICON),
		DASHBOARD("Panel de Control", Paths.FXML_DASHBOARD_SCENE, Paths.DASHBOARD_ICON),
		FLASH_ESP("Flashear ESP32", Paths.FXML_FLASH_ESP32_SCENE, Paths.UPLOAD_ICON),
		ANALYSIS("Análisis de Datos", Paths.FXML_DATA_ANALYSIS_SCENE, Paths.ANALYSIS_ICON),
		FILTERS_SETTINGS("Configuración de Filtros", Paths.FXML_FILTER_SETTINGS_SCENE, Paths.SETTING_ICON);

		private final String title;
		private final String fxmlPath;
		private final String iconPath;

		SceneType(String title, String fxmlPath, String iconPath) {
			this.title = title;
			this.fxmlPath = fxmlPath;
			this.iconPath = iconPath;
		}

		public String getTitle() {
			return title;
		}

		public String getFxmlPath() {
			return fxmlPath;
		}

		public String getIconPath() {
			return iconPath;
		}
	}

	private final Stage stage;
	private SceneType lastScene;

	public SceneManager(Stage stage) {
		this.stage = stage;
	}

	public void switchTo(SceneType sceneType) {
		try {
			FXMLLoader loader = FxmlUtils.loadFXML(sceneType.getFxmlPath());

			loader.setControllerFactory(clazz -> {
				try {
					Object controller = clazz.getDeclaredConstructor().newInstance();
					if (controller instanceof ContextAware) {
						((ContextAware) controller).setAppContext(AppContext.getInstance());
					}
					return controller;
				} catch (Exception e) {
					throw new RuntimeException("Error creando el controlador: " + clazz.getName(), e);
				}
			});

			Parent root = loader.load();
			Scene scene = new Scene(root);

			stage.setScene(scene);
//			stage.setMaximized(true);
//			stage.setFullScreen(true);

			// Cambiar título
			stage.setTitle(sceneType.getTitle());

            // Cambiar icono
            stage.getIcons().clear(); // Limpiar iconos antiguos
            stage.getIcons().add(new Image(getClass().getResourceAsStream(sceneType.getIconPath())));

			this.lastScene = sceneType;

			System.gc();

		} catch (IOException e) {
			System.err.println("Error cargando la escena: " + sceneType.name() + " - " + e.getMessage());
			e.printStackTrace();
		}
	}

	public SceneType getLastScene() {
		return lastScene;
	}

	public Stage getStage() {
		return stage;
	}
}
