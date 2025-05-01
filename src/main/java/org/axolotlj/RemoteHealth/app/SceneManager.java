package org.axolotlj.RemoteHealth.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;

/**
 * Administrador de escenas de la aplicación.
 */
public class SceneManager {

	public enum SceneType {
		DEVICE_SELECTOR("Selector de Dispositivo", "/org/axolotlj/RemoteHealth/fxml/StartupScene.fxml",
				"/org/axolotlj/RemoteHealth/icons/device_selector.png"),
		DEVICE_SETUP("Configuración de Dispositivo", "/org/axolotlj/RemoteHealth/fxml/DeviceSetupScene.fxml",
				"/org/axolotlj/RemoteHealth/icons/device_setup.png"),
		ESP32_TOOLS("Herramientas ESP32", "/org/axolotlj/RemoteHealth/fxml/ESP32ToolsScene.fxml",
				"/org/axolotlj/RemoteHealth/icons/esp32_tools.png"),
		DATA_VIEWER("Visualizador de Datos", "/org/axolotlj/RemoteHealth/fxml/DataViewerScene.fxml",
				"/org/axolotlj/RemoteHealth/icons/data_viewer.png"),
		DASHBOARD("Panel de Control", "/org/axolotlj/RemoteHealth/fxml/DashboardScene.fxml",
				"/org/axolotlj/RemoteHealth/icons/dashboard.png"),
		FLASH_ESP("Flashear ESP32", "/org/axolotlj/RemoteHealth/fxml/FlashEsp32Scene.fxml",
				"/org/axolotlj/RemoteHealth/icons/flash_esp.png"),
		ANALYSIS("Análisis de Datos", "/org/axolotlj/RemoteHealth/fxml/DataAnalysisScene.fxml",
				"/org/axolotlj/RemoteHealth/icons/analysis.png"),
		FILTERS_SETTINGS("Configuración de Filtros", "/org/axolotlj/RemoteHealth/fxml/FilterSettingsScene.fxml",
				"/org/axolotlj/RemoteHealth/icons/filters_settings.png");

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
			FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneType.getFxmlPath()));

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

//            // Cambiar icono
//            stage.getIcons().clear(); // Limpiar iconos antiguos
//            stage.getIcons().add(new Image(getClass().getResourceAsStream(sceneType.getIconPath())));

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
