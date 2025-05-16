package org.axolotlj.RemoteHealth.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import org.apache.commons.lang3.tuple.MutablePair;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.util.DataHandler;
import org.axolotlj.RemoteHealth.util.Paths;
import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.ChartUtils;
import org.axolotlj.RemoteHealth.app.ui.FxmlUtils;
import org.axolotlj.RemoteHealth.config.files.AnalysisFiltersConfig;
import org.axolotlj.RemoteHealth.config.files.RealTimeFiltersConfig;
import org.axolotlj.RemoteHealth.controller.FilterOptionsController.FilterTypeOption;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.filters.AnalysisFilters;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class FilterSettingsController implements ContextAware {

	private AppContext appContext;
	private AnalysisFiltersConfig analysisFiltersConfig;
	private RealTimeFiltersConfig realTimeFiltersConfig;

	private ArrayList<StructureData> structureDatas;
	private ArrayList<MutablePair<Long, Double>> egc;
	private ArrayList<MutablePair<Long, Double>> ir;
	private ArrayList<MutablePair<Long, Double>> red;

	@FXML
	private LineChart<Number, Number> rawChart;
	@FXML
	private LineChart<Number, Number> frequencyRawChart;
	@FXML
	private LineChart<Number, Number> FilteredChart;
	@FXML
	private LineChart<Number, Number> frequencyFilteredChart;

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

		ChartUtils.setStyle(rawChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(FilteredChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(frequencyRawChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(frequencyFilteredChart, Paths.CSS_DASHBOARDSTYLE_CSS);

		try {
			loadFilterOptionsPanes();
		} catch (IOException e) {
			System.err.println("Ocurrio un error al cargar los inculdes ->" + e.getMessage());
		}

		initializeRadioButtons();
		showPane(ecgAnalysisPane);
		loadAndPlotReferenceEcg();

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

		ecgAnalysisController.setType(FilterTypeOption.ECG_ANALYSIS, analysisFiltersConfig, realTimeFiltersConfig);
		plethAnalysisController.setType(FilterTypeOption.PLETH_ANALYSIS, analysisFiltersConfig, realTimeFiltersConfig);
		ecgRealTimeController.setType(FilterTypeOption.ECG_REAL_TIME, analysisFiltersConfig, realTimeFiltersConfig);
		plethRealTimeController.setType(FilterTypeOption.PLETH_REAL_TIME, analysisFiltersConfig, realTimeFiltersConfig);

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

	private void loadAndPlotReferenceEcg() {
		try {
			// 1️⃣ Copiar CSV de recursos a temporal
			Path tmp = Files.createTempFile("ref_", ".csv");
			Files.copy(getClass().getResourceAsStream(Paths.REF_CSV), tmp, StandardCopyOption.REPLACE_EXISTING);

			// 2️⃣ Cargar datos y extraer ECG
			ArrayList<StructureData> list = DataHandler.load(tmp);
			ArrayList<MutablePair<Long, Double>> ecgPairs = DataHandler.extractValidPairs(list,
					DataHandler.SensorField.ECG);

			if (ecgPairs == null || ecgPairs.isEmpty()) {
				System.err.println("No se encontraron datos ECG en el CSV.");
				return;
			}

			// 3️⃣ Filtrar primeros 5 segundos
			long t0 = ecgPairs.get(0).getLeft();
			long tLimit = t0 + 5000; // 5 segundos en ms

			ArrayList<MutablePair<Long, Double>> ecg5s = new ArrayList<>();
			for (MutablePair<Long, Double> p : ecgPairs) {
				if (p.getLeft() <= tLimit) {
					ecg5s.add(p);
				} else {
					break;
				}
			}

			// 4️⃣ Graficar sin filtro
			ChartUtils.plotTimeSeries(rawChart, ecg5s, "ECG (ref)");
			ChartUtils.plotFrequencySeries(frequencyRawChart, ecg5s, "ECG – FFT");

			// 5️⃣ Aplicar filtros y graficar filtrado
			AnalysisFilters analysisFilters = new AnalysisFilters();
			ArrayList<MutablePair<Long, Double>> ecgFiltered = analysisFilters.applyFiltersToEcg(list);

			// También filtrar los datos filtrados a 5s
			ArrayList<MutablePair<Long, Double>> ecgFiltered5s = new ArrayList<>();
			for (MutablePair<Long, Double> p : ecgFiltered) {
				if (p.getLeft() <= tLimit) {
					ecgFiltered5s.add(p);
				} else {
					break;
				}
			}

			ChartUtils.plotTimeSeries(FilteredChart, ecgFiltered5s, "ECG (filtrado)");
			ChartUtils.plotFrequencySeries(frequencyFilteredChart, ecgFiltered5s, "FFT (filtrado)");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
