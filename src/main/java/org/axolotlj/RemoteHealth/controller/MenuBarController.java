package org.axolotlj.RemoteHealth.controller;

import java.util.Locale;

import org.axolotlj.RemoteHealth.app.SceneManager;
import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.config.files.LanguageConfig;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.util.I18n;

import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

public class MenuBarController implements ContextAware{
	private AppContext appContext;
	private SceneManager sceneManager;
	
    @FXML private Menu menuEsp32;
    @FXML private MenuItem itemFlashEsp32;

    @FXML private Menu menuAnalysis;
    @FXML private MenuItem itemAnalyze;

    @FXML private Menu menuFilters;
    @FXML private MenuItem itemFilterSettings;

    @FXML private Menu menuLanguage;
    private final ToggleGroup languageGroup = new ToggleGroup();

    @FXML private RadioMenuItem langSpanish;
    @FXML private RadioMenuItem langEnglish;


    @FXML private Menu menuHelp;
    @FXML private MenuItem itemUserManual;
    @FXML private MenuItem itemAbout;
	
    @FXML
    public void initialize() {
        langSpanish.setToggleGroup(languageGroup);
        langEnglish.setToggleGroup(languageGroup);

        updateTexts();

        languageGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == langSpanish) {
                I18n.setLocale(new Locale("es"));
            } else if (newToggle == langEnglish) {
                I18n.setLocale(Locale.ENGLISH);
            }

            LanguageConfig.saveLocale(I18n.getCurrentLocale()); 
            updateTexts();
        });

        switch (I18n.getCurrentLocale().getLanguage()) {
            case "en" -> langEnglish.setSelected(true);
            default -> langSpanish.setSelected(true);
        }
    }



    private void updateTexts() {
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
	
	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
		this.sceneManager = context.getSceneManager();
	}
	
	
}
