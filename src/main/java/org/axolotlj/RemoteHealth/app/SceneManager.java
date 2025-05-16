package org.axolotlj.RemoteHealth.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.axolotlj.RemoteHealth.app.ui.FxmlUtils;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.core.AppContext.DisposableController;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.util.Paths;

/**
 * Administrador de escenas de la aplicación.
 */
public class SceneManager {

	private final Stage stage;
	private SceneType lastScene;
	private Object currentController;
	private DataLogger dataLogger;

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

	/**
	 * Crea una instancia del administrador de escenas.
	 *
	 * @param stage ventana principal de la aplicación
	 */
	public SceneManager(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Cambia la escena activa a una nueva escena del tipo especificado.
	 *
	 * @param sceneType tipo de escena a mostrar
	 */
	public void switchTo(SceneType sceneType) {
		try {
			if (currentController instanceof DisposableController disposable) {
				dataLogger.logDebug("Cerrando " + currentController.getClass().getSimpleName() + "...");
				disposable.dispose();
			}

			FXMLLoader loader = FxmlUtils.loadFXML(sceneType.getFxmlPath());
			loader.setControllerFactory(clazz -> {
				try {
					Object controller = clazz.getDeclaredConstructor().newInstance();
					if (controller instanceof ContextAware) {
						((ContextAware) controller).setAppContext(AppContext.getInstance());
					}
					return controller;
				} catch (Exception e) {
					dataLogger.logError("SceneManager.switchTo -> Error creando el controlador: " + clazz.getName()
							+ " - " + e.getMessage());
					return null;
				}
			});
			Parent root = loader.load();

			this.currentController = loader.getController();

			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.centerOnScreen();
			stage.setTitle(sceneType.getTitle());
			stage.getIcons().clear();
			stage.getIcons().add(sceneType.getImage());
			this.lastScene = sceneType;

			System.gc();
		} catch (IOException e) {
			dataLogger.logError(
					"SceneManager.switchTo -> Error cargando la escena " + sceneType.name() + ": " + e.getMessage());
		} finally {
			ManagementFactory.getMemoryMXBean().gc();
		}
	}
	
	public void setDataLogger(DataLogger dataLogger) {
		this.dataLogger = dataLogger;
	}

	/**
	 * Obtiene la última escena mostrada.
	 *
	 * @return tipo de la última escena
	 */
	public SceneType getLastScene() {
		return lastScene;
	}

	/**
	 * Obtiene la ventana principal asociada al administrador de escenas.
	 *
	 * @return instancia de Stage
	 */
	public Stage getStage() {
		return stage;
	}
}