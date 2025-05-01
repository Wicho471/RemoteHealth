package org.axolotlj.RemoteHealth.controller;

import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.config.files.AnalysisFiltersConfig;
import org.axolotlj.RemoteHealth.config.files.RealTimeFiltersConfig;
import org.axolotlj.RemoteHealth.controller.FilterOptionsController.FilterTypeOption;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class FilterSettingsController implements ContextAware {

	private static final String FILTER_OPTIONS_PATH = "/org/axolotlj/RemoteHealth/fxml/FilterOptions.fxml";
	
    private AppContext appContext;
    private AnalysisFiltersConfig analysisFiltersConfig;
    private RealTimeFiltersConfig realTimeFiltersConfig;

    @FXML private LineChart<Number, Number> rawChart;
    @FXML private LineChart<Number, Number> frequencyRawChart;
    @FXML private LineChart<Number, Number> FilteredChart;
    @FXML private LineChart<Number, Number> frequencyFilteredChart;

    @FXML private RadioButton ecgAnalysisRadioBtn;
    @FXML private RadioButton ecgRealTimeRadioBtn;
    @FXML private RadioButton plethAnalysisRadioBtn;
    @FXML private RadioButton plethRealTimeRadioBtn;

    @FXML private StackPane oprionsStackPane;

    @FXML private Button returnBtn;

    private ToggleGroup analysisGroup;

    private Node ecgAnalysisPane;
    private Node plethAnalysisPane;
    private Node ecgRealTimePane;
    private Node plethRealTimePane;

    private FilterOptionsController ecgAnalysisController;
    private FilterOptionsController plethAnalysisController;
    private FilterOptionsController ecgRealTimeController;
    private FilterOptionsController plethRealTimeController;

    @FXML
    private void initialize() {
        analysisFiltersConfig = new AnalysisFiltersConfig();
        realTimeFiltersConfig = new RealTimeFiltersConfig();

        try {
            loadFilterOptionsPanes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeRadioButtons();
        showPane(ecgAnalysisPane);
    }

    private void loadFilterOptionsPanes() throws IOException {
        FXMLLoader ecgLoader = loadPane(FILTER_OPTIONS_PATH);
        FXMLLoader plethLoader = loadPane(FILTER_OPTIONS_PATH);
        FXMLLoader ecgRealTimeLoader = loadPane(FILTER_OPTIONS_PATH);
        FXMLLoader plethRealTimeLoader = loadPane(FILTER_OPTIONS_PATH);

        ecgAnalysisPane = ecgLoader.getRoot();
        plethAnalysisPane = plethLoader.getRoot();
        ecgRealTimePane = ecgRealTimeLoader.getRoot();
        plethRealTimePane = plethRealTimeLoader.getRoot();

        oprionsStackPane.getChildren().addAll(
            ecgAnalysisPane, plethAnalysisPane, ecgRealTimePane, plethRealTimePane
        );

        ecgAnalysisPane.setId("ecgAnalysis");
        plethAnalysisPane.setId("plethAnalysis");
        ecgRealTimePane.setId("ecgRealTime");
        plethRealTimePane.setId("plethRealTime");

        ecgAnalysisController = ecgLoader.getController();
        plethAnalysisController = plethLoader.getController();
        ecgRealTimeController = ecgRealTimeLoader.getController();
        plethRealTimeController = plethRealTimeLoader.getController();
        
        ecgAnalysisController.setType(FilterTypeOption.ECG_ANALYSIS);
        plethAnalysisController.setType(FilterTypeOption.PLETH_ANALYSIS);
        ecgRealTimeController.setType(FilterTypeOption.ECG_REAL_TIME);
        plethRealTimeController.setType(FilterTypeOption.PLETH_REAL_TIME);
        
    }

    private FXMLLoader loadPane(String resource) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
        loader.load();
        return loader;
    }

    private void initializeRadioButtons() {
        analysisGroup = new ToggleGroup();
        ecgAnalysisRadioBtn.setToggleGroup(analysisGroup);
        ecgRealTimeRadioBtn.setToggleGroup(analysisGroup);
        plethAnalysisRadioBtn.setToggleGroup(analysisGroup);
        plethRealTimeRadioBtn.setToggleGroup(analysisGroup);

        ecgAnalysisRadioBtn.setSelected(true);

        analysisGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                handleToggleChange((RadioButton) newToggle);
            }
        });
    }

    private void handleToggleChange(RadioButton selectedRadio) {
        if (selectedRadio == ecgAnalysisRadioBtn) {
            showPane(ecgAnalysisPane);
        } else if (selectedRadio == ecgRealTimeRadioBtn) {
            showPane(ecgRealTimePane);
        } else if (selectedRadio == plethAnalysisRadioBtn) {
            showPane(plethAnalysisPane);
        } else if (selectedRadio == plethRealTimeRadioBtn) {
            showPane(plethRealTimePane);
        }
    }

    private void showPane(Node pane) {
        oprionsStackPane.getChildren().forEach(node -> node.setVisible(false));
        pane.setVisible(true);
    }


    @FXML
    private void returnHandle() {
        appContext.getSceneManager().switchTo(SceneType.DEVICE_SELECTOR);
    }

    @Override
    public void setAppContext(AppContext context) {
        this.appContext = context;
    }
}
