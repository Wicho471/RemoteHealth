package org.axolotlj.remotehealth.desktop;

import java.util.Optional;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.CommonApp;
import org.axolotlj.remotehealth.core.config.PlatformConfigurator;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.util.Debug;
import org.axolotlj.remotehealth.desktop.scene.SceneManager;
import org.axolotlj.remotehealth.desktop.scene.SceneType;
import org.axolotlj.remotehealth.desktop.ui.AlertUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Clase principal que lanza la aplicación de escritorio utilizando JavaFX.
 * Se encarga de inicializar la configuración de idioma, contexto de aplicación y escenas.
 */
	
public class DesktopApp extends Application {
	
	@Override
	public void start(Stage stage) {
		Log.get().logInfo("Iniciando renderizado javaFX");

		SceneManager.initialize(stage);
		SceneManager.switchTo(SceneType.DEVICE_SELECTOR);
		stage.show();
		catchEventOnClose(stage);
	}

	@Override
	public void stop() throws Exception {
		AppContext.getInstance().finalize();
		Debug.printAllThreads(false);
	}
	
	private void catchEventOnClose(Stage stage) {
		stage.setOnCloseRequest(event -> {
			event.consume();

			Optional<ButtonType> result = AlertUtil.showConfirmationAlert("Confirmar salida",
					"¿Estás seguro de que deseas salir?", "El programa se cerrará completamente.");

			if (result.isPresent() && result.get() == ButtonType.OK) {
				Platform.exit();
			}
		});
	}

	public static void main(String[] args) {	
		PlatformConfigurator configurator = new DesktopConfigurator();
		configurator.checkPaths();
		configurator.getDeviceInfo();
		configurator.devConfigs();
		
		CommonApp.initialize();

		launch(args);
	}
	
}
