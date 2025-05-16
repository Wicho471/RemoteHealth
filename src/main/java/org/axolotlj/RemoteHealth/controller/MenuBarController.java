package org.axolotlj.RemoteHealth.controller;

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

import org.axolotlj.RemoteHealth.app.SceneManager;
import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.app.ui.FileChooserUtils;
import org.axolotlj.RemoteHealth.config.ConfigFileHelper;
import org.axolotlj.RemoteHealth.config.files.LanguageConfig;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.lang.I18n;
import org.axolotlj.RemoteHealth.lang.LocaleChangeListener;
import org.axolotlj.RemoteHealth.lang.LocaleChangeNotifier;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.service.websocket.WebSocketServerSimulator;
import org.axolotlj.RemoteHealth.service.websocket.WebSocketServerSimulator.GenerationMode;
import org.axolotlj.RemoteHealth.util.Paths;
import org.axolotlj.RemoteHealth.util.TxtUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class MenuBarController implements ContextAware, LocaleChangeListener {
	private AppContext appContext;
	private DataLogger dataLogger;
	private SceneManager sceneManager;
	private WebSocketServerSimulator simu;

	@FXML
	private Menu menuEsp32, menuAnalysis, menuFilters, menuLanguage, menuHelp;
	@FXML
	private MenuItem itemFlashEsp32, itemAnalyze, itemFilterSettings, itemUserManual, itemAbout, simuMenuItem;
	@FXML
	private RadioMenuItem langSpanish, langEnglish, option1Sim, option2Sim;
	@FXML
	private ToggleGroup languageGroup;

	private ToggleGroup simToggleGroup = new ToggleGroup();

	@FXML
	public void initialize() {
		LocaleChangeNotifier.addListener(this);

		langSpanish.setToggleGroup(languageGroup);
		langEnglish.setToggleGroup(languageGroup);

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
		simu.setGenerationMode(GenerationMode.REAL);

		option1Sim.setOnAction(event -> {
			if (option1Sim.isSelected()) {
				simu.setGenerationMode(GenerationMode.REAL);
				simu.restart();
			}
		});

		option2Sim.setOnAction(event -> {
			if (option2Sim.isSelected()) {
				simu.setGenerationMode(GenerationMode.SYNTHETIC);
				simu.restart();
			}
		});

		// También puedes agregar un listener si prefieres
		simToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal == option1Sim) {
				simu.setGenerationMode(GenerationMode.REAL);
			} else if (newVal == option2Sim) {
				simu.setGenerationMode(GenerationMode.SYNTHETIC);
			}
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
		String resourcePath = Paths.DOCS_USERMANUAL_PDF;
		try (InputStream resourceStream = MenuBarController.class.getResourceAsStream(resourcePath)) {
			if (resourceStream == null) {
				throw new FileNotFoundException("Recurso no encontrado: " + resourcePath);
			}

			Optional<File> optionalFile = FileChooserUtils.chooseFile(appContext.getSceneManager().getStage(),
					"Guardar manual de usuario", "Documento PDF", "*.pdf");

			if (optionalFile.isPresent()) {
				File targetFile = optionalFile.get();
				Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

				if (java.awt.Desktop.isDesktopSupported()) {
					java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
					if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
						desktop.open(targetFile);
					} else {
						dataLogger.logError("La acción OPEN no está soportada en esta plataforma.");
					}
				} else {
					dataLogger.logError("Desktop no está soportado en esta plataforma.");
				}
			}

		} catch (IOException e) {
			dataLogger.logError("Error al exportar o abrir PDF desde: " + resourcePath + " -> " + e.getMessage());
		}
	}

	@FXML
	private void aboutHandler() {
		String content = TxtUtils.loadAboutText(Paths.IMG_MISC_ABOUT_TXT);

		Alert alert = AlertUtil.showInformationAlert("Acerca de RemoteHealth", "Información de la aplicación", content, false);

		Text text = new Text(content);
		text.setFont(Font.font("Monospaced", 12));

		TextFlow textFlow = new TextFlow(text);
		textFlow.setPrefWidth(600); // Puedes ajustar el tamaño si quieres

		ScrollPane scrollPane = new ScrollPane(textFlow);
		scrollPane.setFitToWidth(true);
		scrollPane.setPrefHeight(500); // Altura del área visible

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setContent(scrollPane);

		alert.showAndWait();

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
		this.sceneManager = context.getSceneManager();
		this.simu = appContext.getSimulator();
		this.dataLogger = context.getDataLogger();
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
		AlertUtil.showInformationAlert("Modulo en contruccion", null, "Este modulo aun se encuenta en desarollo", true);
	}
}