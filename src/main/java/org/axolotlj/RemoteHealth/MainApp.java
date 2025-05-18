package org.axolotlj.RemoteHealth;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import org.axolotlj.RemoteHealth.app.SceneManager;
import org.axolotlj.RemoteHealth.app.SceneType;
import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.config.files.LanguageConfig;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.lang.I18n;
import org.axolotlj.RemoteHealth.service.logger.*;
import org.axolotlj.RemoteHealth.util.Debug;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Clase principal que lanza la aplicación.
 */
public class MainApp extends Application {

	@Override
	public void start(Stage stage) {
		DataLogger logger = initalizeLogger();
		Log.setLogger(logger);
		logger.logInfo("Iniciando sistema");
		
	    Locale savedLocale = LanguageConfig.loadSavedLocale();
	    I18n.setLocale(savedLocale);

		AppContext.initialize(new SceneManager(stage), logger);
		AppContext.getInstance().getSceneManager().setDataLogger(logger);
		AppContext.getInstance().getSceneManager().switchTo(SceneType.DEVICE_SELECTOR);

		stage.show();
		
		catchEventOnClose(stage);
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
	
	private DataLogger initalizeLogger() {
		try {
			return new FileDataLogger();
		} catch (IOException e) {
			System.err.println("Fallo al inicializar el logger, se usará NoOp: " + e.getMessage());
			return new NoOpDataLogger();
		}
	}

	@Override
	public void stop() throws Exception {
		AppContext.getInstance().finalize();
		Debug.printAllThreads(false);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
