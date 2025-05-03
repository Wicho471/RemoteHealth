package org.axolotlj.RemoteHealth;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import org.axolotlj.RemoteHealth.app.SceneManager;
import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.config.files.LanguageConfig;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.service.logger.*;
import org.axolotlj.RemoteHealth.util.I18n;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Clase principal que lanza la aplicación.
 */
public class MainApp extends Application {

	@Override
	public void start(Stage primaryStage) {
		DataLogger logger;
		try {
			logger = new FileDataLogger();
		} catch (IOException e) {
			System.err.println("Fallo al inicializar el logger, se usará NoOp: " + e.getMessage());
			logger = new NoOpDataLogger();
		}
		logger.logInfo("Iniciando sistema");
		
	    Locale savedLocale = LanguageConfig.loadSavedLocale();
	    I18n.setLocale(savedLocale);

		AppContext.initialize(new SceneManager(primaryStage), logger);
		AppContext.getInstance().getSceneManager().switchTo(SceneType.DEVICE_SELECTOR);
		AppContext.getInstance().getSimulator().start();

		primaryStage.show();

		primaryStage.setOnCloseRequest(event -> {
			event.consume();

			Optional<ButtonType> result = AlertUtil.showConfirmationAlert("Confirmar salida",
					"¿Estás seguro de que deseas salir?", "El programa se cerrará completamente.");

			if (result.isPresent() && result.get() == ButtonType.OK) {
				Platform.exit();
			}
		});
	}

	@Override
	public void stop() throws Exception {
		AppContext.getInstance().finalize();
		System.exit(0);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
