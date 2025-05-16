package org.axolotlj.RemoteHealth.controller;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.axolotlj.RemoteHealth.analysis.DataAnalysis;
import org.axolotlj.RemoteHealth.app.Images;
import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.ChartUtils;
import org.axolotlj.RemoteHealth.app.ui.FileChooserUtils;
import org.axolotlj.RemoteHealth.app.ui.SceneUtils;
import org.axolotlj.RemoteHealth.app.ui.TableUtils;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.core.AppContext.DisposableController;
import org.axolotlj.RemoteHealth.filters.AnalysisFilters;
import org.axolotlj.RemoteHealth.model.AnomalyData;
import org.axolotlj.RemoteHealth.model.ParameterValue;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.util.DataHandler;
import org.axolotlj.RemoteHealth.util.Paths;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DataAnalysisController implements ContextAware, DisposableController {
	// Campos no FXML
	private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private long currentStartTimeMs = 0;
	private long totalDurationMs = 0;
	private AppContext appContext;
	private DataLogger dataLogger;
	private AnalysisFilters filters;
	private ArrayList<StructureData> structureDatas;
	private ArrayList<ParameterValue> processedData;
	private ArrayList<MutablePair<Long, Double>> egc, ir, red;

	// Componentes FXML
	@FXML
	private StackPane rootPane;
	@FXML
	private VBox loadingOverlay;
	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private TableView<AnomalyData> anomalyTable;

	@FXML
	private Button selectFileBtn, openRecHandle;
	@FXML
	private Button startBtn, backwardBtn, nextBtn, endBtn;
	@FXML
	private Button backBtn;

	@FXML
	private ComboBox<String> choiseTimeLenght;
	@FXML
	private TextField initialTimeField, finalTimeField;

	@FXML
	private LineChart<Number, Number> ecgChart, ppgChart, bpmChart, tempChart, spo2Chart, motionChart;

	@FXML
	public void initialize() {
		this.filters = new AnalysisFilters();

		ChartUtils.setStyle(ecgChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(ppgChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(bpmChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(tempChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(spo2Chart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(motionChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		TableUtils.adjustColumns(anomalyTable, false);

		initializeChoiseTimeLength();
		showLoading(false);
	}

	private void initializeChoiseTimeLength() {
		Platform.runLater(() -> {
			choiseTimeLenght.getItems().clear();
			for (int i = 1; i <= 10; i++) {
				choiseTimeLenght.getItems().add(i + " segundos");
			}
			choiseTimeLenght.getSelectionModel().select("10 segundos"); // Por defecto 10s

			choiseTimeLenght.setOnAction(event -> {
				plotAllDataParallel(); // Cada vez que cambia la selección, replotear
			});
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void finalize() throws Throwable {
		executor.shutdown();
		super.finalize();
	}

	@FXML
	private void selectFileHandler() {
		FileChooserUtils.chooseFile(appContext.getSceneManager().getStage(), "Selecciona archivo CSV", "Archivos CSV", "*.csv")
				.map(File::getAbsolutePath).ifPresent(this::loadFile);
	}

	@FXML
	private void startHandle() {
		currentStartTimeMs = 0;
		plotAllDataParallel();
	}

	@FXML
	private void backwardHandle() {
		int seconds = getSelectedSeconds();
		long moveMs = seconds * 1000L;
		if (currentStartTimeMs - moveMs >= 0) {
			currentStartTimeMs -= moveMs;
		} else {
			currentStartTimeMs = 0;
		}
		plotAllDataParallel();
	}

	@FXML
	private void nextHandle() {
		int seconds = getSelectedSeconds();
		long moveMs = seconds * 1000L;
		if (currentStartTimeMs + moveMs < totalDurationMs) {
			currentStartTimeMs += moveMs;
		} else {
			currentStartTimeMs = totalDurationMs - seconds * 1000L;
			if (currentStartTimeMs < 0)
				currentStartTimeMs = 0;
		}
		plotAllDataParallel();
	}

	@FXML
	private void endHandle() {
		int seconds = getSelectedSeconds();
		currentStartTimeMs = totalDurationMs - seconds * 1000L;
		if (currentStartTimeMs < 0)
			currentStartTimeMs = 0;
		plotAllDataParallel();
	}

	@FXML
	private void backHandle() {
		appContext.getSceneManager().switchTo(SceneType.DEVICE_SELECTOR);
	}

	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
		this.dataLogger = context.getDataLogger();
	}

	@FXML
	private void openRecHandle() {
		SceneUtils.openModalWindow(
			    Paths.VIEW_WINDOW_CSVSELECTORWINDOW_FXML,
			    "Seleccionar archivo CSV",
			    this, 
			    Images.IMG_FAVICONS_CSV,
			    msg -> dataLogger.logError(msg)
			);
	}

	public void loadFile(String pathString) {
		dataLogger.logInfo("Leyendo archivo -> " + pathString);
		Path path = java.nio.file.Paths.get(pathString);

		showLoading(true);

		Task<Void> loadTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				if (structureDatas != null) {
					structureDatas.clear();
				}
				structureDatas = DataHandler.load(path);
				egc = filters.applyFiltersToEcg(structureDatas);
				ir = filters
						.applyFiltersToPleth(DataHandler.extractValidPairs(structureDatas, DataHandler.SensorField.IR));
				red = filters.applyFiltersToPleth(
						DataHandler.extractValidPairs(structureDatas, DataHandler.SensorField.RED));

				DataAnalysis dataAnalysis = new DataAnalysis(structureDatas);
				processedData = dataAnalysis.getParameterValues();
				if (egc != null && !egc.isEmpty()) {
					long start = egc.get(0).getLeft();
					long end = egc.get(egc.size() - 1).getLeft();
					totalDurationMs = end - start;
				}

				return null;
			}
		};

		loadTask.setOnSucceeded(event -> {
			currentStartTimeMs = 0;
			plotAllDataParallel();
		});

		loadTask.setOnFailed(event -> {
			showLoading(false);
			Throwable e = loadTask.getException();
			dataLogger.logError("Error al cargar archivo: " + (e != null ? e.getMessage() : "desconocido"));
			e.printStackTrace();
		});

		new Thread(loadTask, "LoadData-Thread").start();
	}

	private void showLoading(boolean show) {
		loadingOverlay.setVisible(show);
		loadingOverlay.setMouseTransparent(!show);
	}

	private void plotAllDataParallel() {
		Task<Void> plotTask = new Task<>() {
			@SuppressWarnings("unchecked")
			@Override
			protected Void call() throws Exception {
				// Ploteo paralelo
				var ecgFuture = executor.submit(() -> buildEcgSeries());
				var plethFuture = executor.submit(() -> buildPlethSeries());
				var tempFuture = executor.submit(() -> buildTempSeries());
				var motionFuture = executor.submit(() -> buildMotionSeries());
				var bpmFuture = executor.submit(() -> buildBpmSeries());
				var spo2Future = executor.submit(() -> buildSpO2Series());

				// Esperamos que terminen
				XYChart.Series<Number, Number> ecgSeries = ecgFuture.get();
				var plethSeries = plethFuture.get(); // par <irSeries, redSeries>

				Platform.runLater(() -> {
					ecgChart.getData().clear();
					ecgChart.getData().add(ecgSeries);
					adjustXAxis(ecgChart);

					ppgChart.getData().clear();
					ppgChart.getData().addAll(plethSeries.getLeft(), plethSeries.getRight());
					adjustXAxis(ppgChart);

					try {
						tempChart.getData().clear();
						tempChart.getData().add(tempFuture.get());
						adjustXAxis(tempChart);

						motionChart.getData().clear();
						motionChart.getData().add(motionFuture.get());
						adjustXAxis(motionChart);

						bpmChart.getData().clear();
						bpmChart.getData().add(bpmFuture.get());
						adjustXAxis(bpmChart);

						spo2Chart.getData().clear();
						spo2Chart.getData().add(spo2Future.get());
						adjustXAxis(spo2Chart);
					} catch (Exception e) {
						e.printStackTrace();
						dataLogger.logError("Error al plotear datos adicionales: " + e.getMessage());
					}

					updateTimeFields();
					showLoading(false);
				});

				return null;
			}
		};

		new Thread(plotTask, "PlotData-Thread").start();
	}

	private XYChart.Series<Number, Number> buildEcgSeries() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("ECG");

		if (egc == null || egc.isEmpty())
			return series;

		long startTime = egc.get(0).getLeft();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		int count = 0;
		for (MutablePair<Long, Double> point : egc) {
			if (point.getLeft() < absoluteStart)
				continue;
			if (point.getLeft() > maxTime || count >= 2500)
				break;
			double relativeTime = (point.getLeft() - startTime) / 1000.0;
			series.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
			count++;
		}

		return series;
	}

	private Pair<XYChart.Series<Number, Number>, XYChart.Series<Number, Number>> buildPlethSeries() {
		XYChart.Series<Number, Number> irSeries = new XYChart.Series<>();
		XYChart.Series<Number, Number> redSeries = new XYChart.Series<>();

		irSeries.setName("IR");
		redSeries.setName("RED");

		int seconds = getSelectedSeconds();

		if (ir != null && !ir.isEmpty()) {
			long startTime = ir.get(0).getLeft(); // inicio absoluto
			long absoluteStart = startTime + currentStartTimeMs;
			long maxTime = absoluteStart + seconds * 1000;

			int count = 0;
			for (MutablePair<Long, Double> point : ir) {
				if (point.getLeft() < absoluteStart)
					continue;
				if (point.getLeft() > maxTime || count >= 2500)
					break;

				double relativeTime = (point.getLeft() - startTime) / 1000.0;
				irSeries.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
				count++;
			}
		}

		if (red != null && !red.isEmpty()) {
			long startTime = red.get(0).getLeft(); // inicio absoluto
			long absoluteStart = startTime + currentStartTimeMs;
			long maxTime = absoluteStart + seconds * 1000;

			int count = 0;
			for (MutablePair<Long, Double> point : red) {
				if (point.getLeft() < absoluteStart)
					continue;
				if (point.getLeft() > maxTime || count >= 2500)
					break;

				double relativeTime = (point.getLeft() - red.get(0).getLeft()) / 1000.0;
				redSeries.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
				count++;
			}
		}

		return Pair.of(irSeries, redSeries);
	}

	private XYChart.Series<Number, Number> buildTempSeries() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("Temp");

		if (processedData == null || processedData.isEmpty())
			return series;

		long startTime = processedData.get(0).getTimeStamp();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (ParameterValue pv : processedData) {
			long ts = pv.getTimeStamp();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;
			double relativeTime = (ts - startTime) / 1000.0;
			if (!Double.isNaN(pv.getTemp())) {
				series.getData().add(new XYChart.Data<>(relativeTime, pv.getTemp()));
			}
		}
		return series;
	}

	private XYChart.Series<Number, Number> buildMotionSeries() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("Movimiento");

		if (processedData == null || processedData.isEmpty())
			return series;

		long startTime = processedData.get(0).getTimeStamp();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (ParameterValue pv : processedData) {
			long ts = pv.getTimeStamp();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;
			double relativeTime = (ts - startTime) / 1000.0;
			if (!Double.isNaN(pv.getMov())) {
				series.getData().add(new XYChart.Data<>(relativeTime, pv.getMov()));
			}
		}
		return series;
	}

	private XYChart.Series<Number, Number> buildBpmSeries() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("BPM");

		if (processedData == null || processedData.isEmpty())
			return series;

		long startTime = processedData.get(0).getTimeStamp();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (ParameterValue pv : processedData) {
			long ts = pv.getTimeStamp();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;
			double relativeTime = (ts - startTime) / 1000.0;
			series.getData().add(new XYChart.Data<>(relativeTime, pv.getBpm()));
		}
		return series;
	}

	private XYChart.Series<Number, Number> buildSpO2Series() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("SpO2");

		if (processedData == null || processedData.isEmpty())
			return series;

		long startTime = processedData.get(0).getTimeStamp();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (ParameterValue pv : processedData) {
			long ts = pv.getTimeStamp();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;
			double relativeTime = (ts - startTime) / 1000.0;
			if (!Double.isNaN(pv.getSpO2())) {
				series.getData().add(new XYChart.Data<>(relativeTime, pv.getSpO2()));
			}
		}
		return series;
	}

	private int getSelectedSeconds() {
		String selected = choiseTimeLenght.getSelectionModel().getSelectedItem();
		if (selected != null && selected.contains("segundos")) {
			return Integer.parseInt(selected.split(" ")[0]);
		}
		return 1; // Default por si acaso
	}

	private void updateTimeFields() {
		int secondsWindow = getSelectedSeconds();
		initialTimeField.setText(String.format("%.2f", currentStartTimeMs / 1000.0) + " s");
		finalTimeField.setText(String.format("%.2f", (currentStartTimeMs / 1000.0 + secondsWindow)) + " s");
	}

	private void adjustXAxis(LineChart<Number, Number> chart) {
		long start = egc.get(0).getLeft();
		long currentStart = start + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		double minX = (currentStart - start) / 1000.0;
		double maxX = minX + seconds;
		double tickUnit = seconds / 5.0;

		ChartUtils.configureXAxis(chart, maxX, minX, tickUnit, false);
	}

	@Override
	public void dispose() {
		
	}

}
