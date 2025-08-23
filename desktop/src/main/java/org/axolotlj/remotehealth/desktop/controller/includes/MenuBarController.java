package org.axolotlj.remotehealth.desktop.controller.includes;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.AppContext.ContextAware;
import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.config.files.LanguageConfig;
import org.axolotlj.remotehealth.core.lang.I18n;
import org.axolotlj.remotehealth.core.lang.LocaleChangeListener;
import org.axolotlj.remotehealth.core.lang.LocaleChangeNotifier;
import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.path.SharedPaths;
import org.axolotlj.remotehealth.core.simulation.GenerationMode;
import org.axolotlj.remotehealth.desktop.scene.SceneManager;
import org.axolotlj.remotehealth.desktop.scene.SceneType;
import org.axolotlj.remotehealth.desktop.service.websocket.WebSocketServerSimulator;
import org.axolotlj.remotehealth.desktop.ui.AlertUtil;
import org.axolotlj.remotehealth.desktop.ui.DialogPanelUtils;
import org.axolotlj.remotehealth.desktop.ui.FileChooserUtils;
import org.axolotlj.remotehealth.desktop.ui.SceneUtils;
import org.axolotlj.remotehealth.desktop.utils.DesktopPaths;
import org.axolotlj.remotehealth.desktop.utils.Images;

import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

public class MenuBarController implements ContextAware, LocaleChangeListener {
	private AppContext appContext;
	private DataLogger dataLogger = Log.get();
	private SceneManager sceneManager;
	private WebSocketServerSimulator simu;

	@FXML
	private Menu menuEsp32, menuAnalysis, menuFilters, menuLanguage, menuHelp;
	@FXML
	private MenuItem itemFlashEsp32, itemAnalyze, itemFilterSettings, itemUserManual, itemAbout, simuMenuItem;
	@FXML
	private RadioMenuItem langSpanish, langEnglish, option1Sim, option2Sim, dev;
	@FXML
	private ToggleGroup languageGroup, simToggleGroup;

	@FXML
	public void initialize() {
		LocaleChangeNotifier.addListener(this);

		onLocaleChanged();
		languageGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
			if (newToggle == langSpanish) {
				I18n.setLocale(new Locale("es"));
			} else if (newToggle == langEnglish) {
				I18n.setLocale(Locale.ENGLISH);
			}

			LanguageConfig.saveLocale(I18n.getCurrentLocale());
			LocaleChangeNotifier.notifyLocaleChanged();
		});

		switch (I18n.getCurrentLocale().getLanguage()) {
		case "en" -> langEnglish.setSelected(true);
		default -> langSpanish.setSelected(true);
		}

		// Configura el grupo manualmente
		option1Sim.setToggleGroup(simToggleGroup);
		option2Sim.setToggleGroup(simToggleGroup);

		// Selección por defecto (opcional)
		option1Sim.setSelected(true);

		switch (WebSocketServerSimulator.generationMode) {
		case REAL:
			option1Sim.setSelected(true);
			break;

		case SYNTHETIC:
			option2Sim.setSelected(true);
			break;

		default:
			break;
		}

		simToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
		    if (newVal == option1Sim) {
		        simu.setGenerationMode(GenerationMode.REAL);
		    } else if (newVal == option2Sim) {
		        simu.setGenerationMode(GenerationMode.SYNTHETIC);
		    }
		    simu.restart();
		});
		
		simuMenuItem.setText(simu.isActive() ? "Detener simulador" : "Iniciar simulador");
		
		dev.setSelected(appContext.getGeneralConfig().isDeveloperMode());
		dev.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
		    appContext.getGeneralConfig().setDeveloperMode(isSelected);
		});
	}

	@FXML
	private void openAnalysisHandler() {
		sceneManager.switchTo(SceneType.ANALYSIS);
	}

	@FXML
	private void flashEsp32Handle() {
		sceneManager.switchTo(SceneType.FLASH_ESP);
	}

	@FXML
	private void filterSettingsHandler() {
		sceneManager.switchTo(SceneType.FILTERS_SETTINGS);
	}

	@FXML
	private void userManualHandler() {
		String resourcePath = SharedPaths.DOCS_USERMANUAL_PDF;
		try (InputStream resourceStream = MenuBarController.class.getResourceAsStream(resourcePath)) {
			if (resourceStream == null) {
				throw new FileNotFoundException("Recurso no encontrado: " + resourcePath);
			}

			Optional<File> optionalFile = FileChooserUtils.chooseSaveLocation(SceneManager.getStage(),
					"Guardar manual de usuario", "Documento PDF", "*.pdf", "manual_usuario.pdf");

			if (optionalFile.isPresent()) {
				File targetFile = optionalFile.get();
				Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

				if (java.awt.Desktop.isDesktopSupported()) {
					java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
					if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
						desktop.open(targetFile);
					} else {
						dataLogger.logWarn("La acción OPEN no está soportada en esta plataforma.");
					}
				} else {
					dataLogger.logWarn("Desktop no está soportado en esta plataforma.");
				}
			}

		} catch (IOException e) {
			dataLogger.logException("Error al exportar o abrir PDF desde: '" + resourcePath + "'", e);
		}
	}

	@FXML
	private void aboutHandler() {
	    DialogPanelUtils.showTextDialog("Acerca de RemoteHealth", "Información de la aplicación", SharedPaths.ABOUT_TXT, 600, 650, true, "Monospaced", 12);
	}
	
	@FXML
	private void licenseHandler() {
		DialogPanelUtils.showTextDialog("Licencia", "Información de la licencia", SharedPaths.LICENSE_TXT, 500, 500, false, "Monospaced", 12);
	}
	
	@FXML
	private void creditHandle() {
		AlertUtil.showInformationAlert("Creditos", null, "---", true);
	}

	@FXML
	private void thanksHandle() {
		DialogPanelUtils.showTextDialog("Agradecimientos especiales", "Reconocimiento al Ing. Esaúl Trujillo Islas", SharedPaths.THANKS_TXT, 500, 400, false, "Monospaced", 14);
	}

	@FXML
	private void logHandle() {
		SceneUtils.openModalWindow(DesktopPaths.VIEW_WINDOW_LOG_FXML, "Visualizador de registros", this, Images.IMG_FAVICONS_LOG);
	}
	
	@FXML
	public void simuHandle() {
		if (!simu.isActive()) {
			simu.start();
			simuMenuItem.setText("Detener simulador");
		} else {
			try {
				simu.stop();
				simuMenuItem.setText("Iniciar simulador");
			} catch (Exception e) {
				AlertUtil.showErrorAlert("Error", "Problemas con el simulador", e.getMessage());
			}
		}
	}

	@FXML
	private void dirHandler() {
		Path path = ConfigFileHelper.resolveMainDir();
		try {
			File file = new File(path.toAbsolutePath().toString());
			if (!file.exists()) {
				System.err.println("La ruta no existe: " + path);
				return;
			}
			Desktop desktop = Desktop.getDesktop();
			desktop.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
		this.simu = (WebSocketServerSimulator) appContext.getSimulator();
	}

	@Override
	public void onLocaleChanged() {
		menuEsp32.setText(I18n.get("menu.esp32"));
		itemFlashEsp32.setText(I18n.get("menu.esp32.flash"));

		menuAnalysis.setText(I18n.get("menu.analysis"));
		itemAnalyze.setText(I18n.get("menu.analysis.analyze"));

		menuFilters.setText(I18n.get("menu.filters"));
		itemFilterSettings.setText(I18n.get("menu.filters.settings"));

		menuLanguage.setText(I18n.get("menu.language"));
		langSpanish.setText(I18n.get("menu.language.spanish"));
		langEnglish.setText(I18n.get("menu.language.english"));

		menuHelp.setText(I18n.get("menu.help"));
		itemUserManual.setText(I18n.get("menu.help.manual"));
		itemAbout.setText(I18n.get("menu.help.about"));
	}

	@FXML
	private void benchmarkHandle() {
    	AlertUtil.buildingModule();
	}
}