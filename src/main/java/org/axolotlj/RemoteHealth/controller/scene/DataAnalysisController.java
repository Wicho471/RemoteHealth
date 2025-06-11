package org.axolotlj.RemoteHealth.controller.scene;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.axolotlj.RemoteHealth.app.SceneType;
import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.app.ui.ChartUtils;
import org.axolotlj.RemoteHealth.app.ui.FileChooserUtils;
import org.axolotlj.RemoteHealth.app.ui.SceneUtils;
import org.axolotlj.RemoteHealth.app.ui.TableUtils;
import org.axolotlj.RemoteHealth.common.Images;
import org.axolotlj.RemoteHealth.common.Paths;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.core.AppContext.DisposableController;
import org.axolotlj.RemoteHealth.model.AnalysisData;
import org.axolotlj.RemoteHealth.model.AnomalyData;
import org.axolotlj.RemoteHealth.sensor.io.CsvDataManager;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DataAnalysisController implements ContextAware, DisposableController {
	// Campos no FXML
	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private long currentStartTimeMs = 0;
	private long totalDurationMs = 0;
	private AppContext appContext;
	private DataLogger dataLogger;
	private AnalysisData analysisData;

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
	private LineChart<Number, Number> ecgChart, ppgChart, bpmChart, tempChart, spo2Chart, motionChart, bpChart;

	@FXML
	public void initialize() {
		ChartUtils.setStyle(ecgChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(ppgChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(bpmChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(tempChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(spo2Chart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(motionChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(bpChart, Paths.CSS_DASHBOARDSTYLE_CSS);
		TableUtils.adjustColumns(anomalyTable, false);

		initializeChoiseTimeLength();
		showLoading(false);
	}

	private void initializeChoiseTimeLength() {
		Platform.runLater(() -> {
			choiseTimeLenght.getItems().clear();
			for (int i = 1; i <= 5; i++) {
				choiseTimeLenght.getItems().add(i + " segundos");
			}
			choiseTimeLenght.getSelectionModel().select("5 segundos"); // Por defecto 10s

			choiseTimeLenght.setOnAction(event -> {
				plotAllDataParallel(); // Cada vez que cambia la selección, replotear
			});
		});
	}

	@FXML
	private void selectFileHandler() {
		FileChooserUtils
				.chooseFile(appContext.getSceneManager().getStage(), "Selecciona archivo CSV", "Archivos CSV", "*.csv")
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
		SceneUtils.openModalWindow(Paths.VIEW_WINDOW_CSVSELECTORWINDOW_FXML, "Seleccionar archivo CSV", this,
				Images.IMG_FAVICONS_CSV, msg -> dataLogger.logError(msg));
	}
	
	@FXML
	private void analysisHandle() {
		AlertUtil.buildingModule();
	}

	@FXML
	private void downloadInfoHandle() {
		AlertUtil.buildingModule();
	}

	public void loadFile(String pathString) {
		dataLogger.logInfo("Leyendo archivo -> " + pathString);
		Path path = java.nio.file.Paths.get(pathString);

		showLoading(true);

		Task<Void> loadTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				if (analysisData != null) {
					analysisData.clear();
				}

				analysisData = new AnalysisData(CsvDataManager.load(path));
				analysisData.calculateSigns();
				ArrayList<MutablePair<Long, Double>> egc = analysisData.getEgc();
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
				var systolicFuture = executor.submit(() -> buildSystolicSeries());
				var diastolicFuture = executor.submit(() -> buildDiastolicSeries());

				// Esperamos que terminen
				XYChart.Series<Number, Number> ecgSeries = ecgFuture.get();
				var plethSeries = plethFuture.get(); // par <irSeries, redSeries>

				Platform.runLater(() -> {
					ecgChart.getData().clear();
					ecgChart.getData().add(ecgSeries);
					adjustXAxis(ecgChart);
					attachTooltips(ecgChart);

					ppgChart.getData().clear();
					ppgChart.getData().addAll(plethSeries.getLeft(), plethSeries.getRight());
					adjustXAxis(ppgChart);
					attachTooltips(ppgChart);

					try {
						tempChart.getData().clear();
						tempChart.getData().add(tempFuture.get());
						adjustXAxis(tempChart);

						motionChart.getData().clear();
						motionChart.getData().add(motionFuture.get());
						adjustXAxis(motionChart);
//
						bpmChart.getData().clear();
						bpmChart.getData().add(bpmFuture.get());
						adjustXAxis(bpmChart);
//
						spo2Chart.getData().clear();
						spo2Chart.getData().add(spo2Future.get());
						adjustXAxis(spo2Chart);
						
						bpChart.getData().clear();
						bpChart.getData().addAll(systolicFuture.get(), diastolicFuture.get());
						adjustXAxis(bpChart);
						attachTooltips(bpChart);
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
		ArrayList<MutablePair<Long, Double>> egc = analysisData.getEgc();
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
		ArrayList<MutableTriple<Long, Double, Double>> pleth = analysisData.getPleth();
		if (pleth == null || pleth.isEmpty()) {
			return Pair.of(irSeries, redSeries);
		}

		long startTime = pleth.get(0).getLeft(); // timestamp base
		long absoluteStart = startTime + currentStartTimeMs;
		long maxTime = absoluteStart + seconds * 1000;

		int irCount = 0;
		int redCount = 0;

		for (MutableTriple<Long, Double, Double> point : pleth) {
			long timestamp = point.getLeft();

			if (timestamp < absoluteStart)
				continue;
			if (timestamp > maxTime || (irCount >= 2500 && redCount >= 2500))
				break;

			double relativeTime = (timestamp - startTime) / 1000.0;

			if (checkValidDouble(point.getMiddle()) && irCount < 2500) {
				irSeries.getData().add(new XYChart.Data<>(relativeTime, point.getMiddle()));
				irCount++;
			}

			if (checkValidDouble(point.getRight()) && redCount < 2500) {
				redSeries.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
				redCount++;
			}
		}

		return Pair.of(irSeries, redSeries);
	}

	private boolean checkValidDouble(double value) {
		return !Double.isNaN(value) && !Double.isInfinite(value);
	}

	private XYChart.Series<Number, Number> buildTempSeries() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("Temperatura");

		ArrayList<MutablePair<Long, Double>> tempData = analysisData.getTemp();
		if (tempData == null || tempData.isEmpty())
			return series;

		long startTime = tempData.get(0).getLeft();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (MutablePair<Long, Double> point : tempData) {
			long ts = point.getLeft();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;

			double relativeTime = (ts - startTime) / 1000.0;
			if (checkValidDouble(point.getRight())) {
				series.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
			}
		}
		return series;
	}

//
	private XYChart.Series<Number, Number> buildMotionSeries() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("Movimiento");

		ArrayList<MutablePair<Long, Double>> motionData = analysisData.getMov();
		if (motionData == null || motionData.isEmpty())
			return series;

		long startTime = motionData.get(0).getLeft();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (MutablePair<Long, Double> point : motionData) {
			long ts = point.getLeft();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;

			double relativeTime = (ts - startTime) / 1000.0;
			if (checkValidDouble(point.getRight())) {
				series.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
			}
		}
		return series;
	}

//
	private XYChart.Series<Number, Number> buildBpmSeries() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("BPM");

		ArrayList<MutablePair<Long, Integer>> bpmData = analysisData.getHr();
		if (bpmData == null || bpmData.isEmpty())
			return series;

		long startTime = bpmData.get(0).getLeft();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (MutablePair<Long, Integer> point : bpmData) {
			long ts = point.getLeft();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;
			double relativeTime = (ts - startTime) / 1000.0;
			series.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
		}

		return series;
	}

	private XYChart.Series<Number, Number> buildSpO2Series() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("SpO₂");

		ArrayList<MutablePair<Long, Integer>> spo2Data = analysisData.getSpo2();
		if (spo2Data == null || spo2Data.isEmpty())
			return series;

		long startTime = spo2Data.get(0).getLeft();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (MutablePair<Long, Integer> point : spo2Data) {
			long ts = point.getLeft();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;
			double relativeTime = (ts - startTime) / 1000.0;
			series.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
		}

		return series;
	}

	private XYChart.Series<Number, Number> buildSystolicSeries() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("Sistólica");

		ArrayList<MutableTriple<Long, Double, Double>> bpData = analysisData.getBp();
		if (bpData == null || bpData.isEmpty())
			return series;

		long startTime = bpData.get(0).getLeft();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (MutableTriple<Long, Double, Double> point : bpData) {
			long ts = point.getLeft();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;

			double relativeTime = (ts - startTime) / 1000.0;
			series.getData().add(new XYChart.Data<>(relativeTime, point.getMiddle()));
		}

		return series;
	}

	private XYChart.Series<Number, Number> buildDiastolicSeries() {
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName("Diastólica");

		ArrayList<MutableTriple<Long, Double, Double>> bpData = analysisData.getBp();
		if (bpData == null || bpData.isEmpty())
			return series;

		long startTime = bpData.get(0).getLeft();
		long absoluteStart = startTime + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		long maxTime = absoluteStart + seconds * 1000;

		for (MutableTriple<Long, Double, Double> point : bpData) {
			long ts = point.getLeft();
			if (ts < absoluteStart)
				continue;
			if (ts > maxTime)
				break;

			double relativeTime = (ts - startTime) / 1000.0;
			series.getData().add(new XYChart.Data<>(relativeTime, point.getRight()));
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
		long start = analysisData.getEgc().get(0).getLeft();
		long currentStart = start + currentStartTimeMs;
		int seconds = getSelectedSeconds();
		double minX = (currentStart - start) / 1000.0;
		double maxX = minX + seconds;
		double tickUnit = seconds / 5.0;

		ChartUtils.configureXAxis(chart, maxX, minX, tickUnit, false);
	}

	private void attachTooltips(LineChart<Number, Number> chart) {
		// mostrarlos sin retardo
		Tooltip t = new Tooltip(); // plantillas reutilizables
		t.setShowDelay(Duration.ZERO);

		for (XYChart.Series<Number, Number> s : chart.getData()) {
			for (XYChart.Data<Number, Number> d : s.getData()) {
				String text = String.format("%s\nTiempo: %.3f s\nValor: %.3f", s.getName(), d.getXValue().doubleValue(),
						d.getYValue().doubleValue());

				Tooltip tip = new Tooltip(text);
				tip.setShowDelay(Duration.ZERO);

				if (d.getNode() != null) {
					Tooltip.install(d.getNode(), tip);
				} else {
					// Esperar a que el nodo esté disponible
					d.nodeProperty().addListener((obs, oldNode, newNode) -> {
						if (newNode != null) {
							Tooltip.install(newNode, tip);
						}
					});
				}
			}
		}
	}

	@Override
	public void dispose() {
		if (executor != null) {
			executor.shutdown();
			executor = null;
		}
		if (analysisData != null) {
			analysisData.clear();
		}
	}

}
