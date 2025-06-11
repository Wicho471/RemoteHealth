package org.axolotlj.RemoteHealth.controller.scene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.axolotlj.RemoteHealth.sensor.TuplaUtil;
import org.axolotlj.RemoteHealth.sensor.data.DataPoint;
import org.axolotlj.RemoteHealth.sensor.handle.DataExtractor;
import org.axolotlj.RemoteHealth.sensor.handle.DataHandler;
import org.axolotlj.RemoteHealth.sensor.handle.SensorField;
import org.axolotlj.RemoteHealth.sensor.io.CsvDataManager;
import org.axolotlj.RemoteHealth.app.SceneType;
import org.axolotlj.RemoteHealth.app.ui.ChartUtils;
import org.axolotlj.RemoteHealth.app.ui.FxmlUtils;
import org.axolotlj.RemoteHealth.common.Paths;
import org.axolotlj.RemoteHealth.config.filt.AnalysisFiltersConfig;
import org.axolotlj.RemoteHealth.config.filt.RealTimeFiltersConfig;
import org.axolotlj.RemoteHealth.controller.include.FilterOptionsController;
import org.axolotlj.RemoteHealth.controller.include.FilterTypeOption;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.core.AppContext.DisposableController;
import org.axolotlj.RemoteHealth.filters.AnalysisFilters;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class FilterSettingsController implements ContextAware, DisposableController {
	
	private AppContext appContext;
	private AnalysisFiltersConfig analysisFiltersConfig;
	private RealTimeFiltersConfig realTimeFiltersConfig;

	private ArrayList<MutablePair<Long, Double>> ecgData;
	private ArrayList<MutableTriple<Long, Double, Double>> plethData;

	private ArrayList<MutablePair<Long, Double>> ecgFilterPairs;
	private ArrayList<MutableTriple<Long, Double, Double>> plethFilterPairs;
	
	private ArrayList<MutablePair<Long, Double>> zoomedEcgData;
	private ArrayList<MutablePair<Long, Double>> zoomedEcgFiltered;
	private ArrayList<MutablePair<Long, Double>> zoomedPlethData;
	private ArrayList<MutablePair<Long, Double>> zoomedPlethFiltered;

	@FXML
	private LineChart<Number, Number> dataChart;
	@FXML
	private LineChart<Number, Number> frequencyChart;

	@FXML
	private RadioButton ecgAnalysisRadioBtn;
	@FXML
	private RadioButton ecgRealTimeRadioBtn;
	@FXML
	private RadioButton plethAnalysisRadioBtn;
	@FXML
	private RadioButton plethRealTimeRadioBtn;

	@FXML
	private StackPane oprionsStackPane;

	@FXML
	private Button returnBtn;

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

		ChartUtils.setStyle(dataChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(frequencyChart, Paths.CSS_DASHBOARDSTYLE_CSS);

		try {
			loadFilterOptionsPanes();
		} catch (IOException e) {
			System.err.println("Ocurrio un error al cargar los inculdes ->" + e.getMessage());
		}
		loadRawData();

		initializeRadioButtons();
		showPane(ecgAnalysisPane);
	}

	private void loadRawData() {
		try {
			Path tmp = Files.createTempFile("ref_", ".csv");

			Files.copy(getClass().getResourceAsStream(Paths.REF_CSV), tmp, StandardCopyOption.REPLACE_EXISTING);

			ArrayList<DataPoint> dataPoints = CsvDataManager.load(tmp);
			ecgData = DataExtractor.extractValidValues(dataPoints, SensorField.ECG);
			fix(ecgData);
			plethData = DataExtractor.extractValidValues(dataPoints, SensorField.IR, SensorField.RED);
			fixTiple(plethData);
			applyChanges(FilterTypeOption.ECG_ANALYSIS);
		} catch (IOException e) {
			System.err.println("Error al acceder o copiar el archivo CSV:");
		} catch (Exception e) {
			System.err.println("Error al cargar o procesar los datos del CSV:");
		}
	}

	private void applyChanges(FilterTypeOption option) {
	    AnalysisFilters analysisFilters = new AnalysisFilters();

	    ecgFilterPairs = analysisFilters.getFilteredEcg(ecgData);
	    plethFilterPairs = analysisFilters.getFilteredPleth(plethData);

	    zoomedEcgData = DataHandler.zoomPairs(ecgData, 5000, 5000);
	    zoomedEcgFiltered = DataHandler.zoomPairs(ecgFilterPairs, 5000, 5000);

	    var rawPleth = TuplaUtil.createTupla(
	        TuplaUtil.extractTimestamps(plethData),
	        TuplaUtil.extractMiddleValues(plethData)
	    );
	    var filteredPleth = TuplaUtil.createTupla(
	        TuplaUtil.extractTimestamps(plethFilterPairs),
	        TuplaUtil.extractMiddleValues(plethFilterPairs)
	    );
	    zoomedPlethData = DataHandler.zoomPairs(rawPleth, 5000, 5000);
	    zoomedPlethFiltered = DataHandler.zoomPairs(filteredPleth, 5000, 5000);

	    plotChart(option);
	}

	
	private void plotChart(FilterTypeOption option) {
	    switch (option) {
	        case ECG_ANALYSIS -> {
	            ChartUtils.plotTwoTimeSeriesFromPairs(dataChart, zoomedEcgData, zoomedEcgFiltered, "Raw ECG", "Filtered ECG");
	            ChartUtils.plotTwoFrequencySeriesFromPairs(frequencyChart, zoomedEcgData, zoomedEcgFiltered, "Raw ECG", "Filtered ECG");
	        }
	        case PLETH_ANALYSIS -> {
	            ChartUtils.plotTwoTimeSeriesFromPairs(dataChart, zoomedPlethData, zoomedPlethFiltered, "Raw Pleth", "Filtered Pleth");
	            ChartUtils.plotTwoFrequencySeriesFromPairs(frequencyChart, zoomedPlethData, zoomedPlethFiltered, "Raw Pleth", "Filtered Pleth");
	        }
	        default -> throw new IllegalArgumentException("Unexpected value: " + option);
	    }
	}


	private void loadFilterOptionsPanes() throws IOException {
		// ECG análisis
		FXMLLoader ecgLoader = FxmlUtils.loadFXML(Paths.VIEW_INCLUDE_FILTEROPTIONS_FXML);
		ecgAnalysisPane = ecgLoader.load();
		ecgAnalysisController = ecgLoader.getController();

		FXMLLoader plethLoader = FxmlUtils.loadFXML(Paths.VIEW_INCLUDE_FILTEROPTIONS_FXML);
		plethAnalysisPane = plethLoader.load();
		plethAnalysisController = plethLoader.getController();

		FXMLLoader ecgRTLoader = FxmlUtils.loadFXML(Paths.VIEW_INCLUDE_FILTEROPTIONS_FXML);
		ecgRealTimePane = ecgRTLoader.load();
		ecgRealTimeController = ecgRTLoader.getController();

		FXMLLoader plethRTLoader = FxmlUtils.loadFXML(Paths.VIEW_INCLUDE_FILTEROPTIONS_FXML);
		plethRealTimePane = plethRTLoader.load();
		plethRealTimeController = plethRTLoader.getController();

		oprionsStackPane.getChildren().addAll(ecgAnalysisPane, plethAnalysisPane, ecgRealTimePane, plethRealTimePane);

		ecgAnalysisController.setType(FilterTypeOption.ECG_ANALYSIS, analysisFiltersConfig, realTimeFiltersConfig, tipo -> this.applyChanges(tipo));
		plethAnalysisController.setType(FilterTypeOption.PLETH_ANALYSIS, analysisFiltersConfig, realTimeFiltersConfig, tipo -> this.applyChanges(tipo));
		ecgRealTimeController.setType(FilterTypeOption.ECG_REAL_TIME, analysisFiltersConfig, realTimeFiltersConfig, tipo -> this.applyChanges(tipo));
		plethRealTimeController.setType(FilterTypeOption.PLETH_REAL_TIME, analysisFiltersConfig, realTimeFiltersConfig, tipo -> this.applyChanges(tipo));

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
				analysisFiltersConfig.loadProperties();
				realTimeFiltersConfig.loadProperties();
			}
		});
	}

	private void handleToggleChange(RadioButton selectedRadio) {
		if (selectedRadio == ecgAnalysisRadioBtn) {
			showPane(ecgAnalysisPane);
			plotChart(FilterTypeOption.ECG_ANALYSIS);
		} else if (selectedRadio == ecgRealTimeRadioBtn) {
			showPane(ecgRealTimePane);
		} else if (selectedRadio == plethAnalysisRadioBtn) {
			showPane(plethAnalysisPane);
			plotChart(FilterTypeOption.PLETH_ANALYSIS);
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

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}
	
	private void fix(ArrayList<MutablePair<Long, Double>> pairs) {
	    for (MutablePair<Long, Double> mutablePair : pairs) {
	        mutablePair.setRight(mutablePair.getRight() - 2000);
	    }
	}
	
	private void fixTiple(ArrayList<MutableTriple<Long, Double, Double>> triple) {
	    for (MutableTriple<Long, Double, Double> mutableTriple : triple) {
	        mutableTriple.setRight(mutableTriple.getRight() - 121250);
	        mutableTriple.setMiddle(mutableTriple.getMiddle() - 121250);
	    }
	}
}
