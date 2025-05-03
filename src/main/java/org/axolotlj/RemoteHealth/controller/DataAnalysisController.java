package org.axolotlj.RemoteHealth.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.ChartUtils;
import org.axolotlj.RemoteHealth.app.ui.TableUtils;
import org.axolotlj.RemoteHealth.config.ConfigFileHelper;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.filters.AnalysisFilters;
import org.axolotlj.RemoteHealth.model.AnomalyData;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.util.DataHandler;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class DataAnalysisController implements ContextAware {

	private long currentStartTimeMs = 0;
	private long totalDurationMs = 0;
	private AppContext appContext;
	private DataLogger dataLogger;
	private ArrayList<StructureData> structureDatas;
	private ArrayList<MutablePair<Long, Double>> egc;
	private ArrayList<MutablePair<Long, Double>> ir;
	private ArrayList<MutablePair<Long, Double>> red;
	private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private AnalysisFilters filters;

	// Tabla de anomalías
	@FXML
	private TableView<AnomalyData> anomalyTable;

	// Controles para selección de archivos
	@FXML
	private ComboBox<String> choiseFile;
	@FXML
	private Button selectFileBtn;

	// Controles para selección de tiempo
	@FXML
	private ComboBox<String> choiseTimeLenght;
	@FXML
	private TextField initialTimeField;
	@FXML
	private TextField finalTimeField;

	// Controles de navegación temporal
	@FXML
	private Button startBtn;
	@FXML
	private Button backwardBtn;
	@FXML
	private Button nextBtn;
	@FXML
	private Button endBtn;

	// Botón de regreso
	@FXML
	private Button backBtn;

	// Gráficas
	@FXML
	private LineChart<Number, Number> ecgChart;
	@FXML
	private LineChart<Number, Number> ppgChart;
	@FXML
	private LineChart<Number, Number> bpmChart;
	@FXML
	private LineChart<Number, Number> tempChart;
	@FXML
	private LineChart<Number, Number> spo2Chart;
	@FXML
	private LineChart<Number, Number> motionChart;

	@FXML
	private StackPane rootPane;

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private VBox loadingOverlay;

	@FXML
	public void initialize() {
		this.filters = new AnalysisFilters();

		TableUtils.adjustColumns(anomalyTable, false);
		initializeChoiseTimeLength();

		Task<Void> initTask = new Task<>() {
			@Override
			protected Void call() {
				loadChoiseFile();
				return null;
			}
		};

		initTask.setOnSucceeded(event -> {
			showLoading(false);
		});

		initTask.setOnFailed(event -> {
			showLoading(false);
			Throwable e = initTask.getException();
			if (e != null) {
				e.printStackTrace();
				dataLogger.logError("Error al inicializar DataAnalysisController: " + e.getMessage());
			}
		});

		showLoading(true);
		new Thread(initTask, "InitDataAnalysis-Thread").start();
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
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
		File file = fileChooser.showOpenDialog(appContext.getSceneManager().getStage());
		if (file != null) {
			String pathFile = file.getAbsolutePath();
			loadFile(pathFile);
		}
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

	private void loadChoiseFile() {
		Path folderPath = ConfigFileHelper.getDataDir();

		File folder = folderPath.toFile();
		if (!folder.exists() || !folder.isDirectory()) {
			dataLogger.logError("Directorio inválido: " + folderPath);
			return;
		}

		File[] csvFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
		if (csvFiles == null || csvFiles.length == 0) {
			dataLogger.logInfo("No se encontraron archivos CSV en: " + folderPath);
			return;
		}

		Platform.runLater(() -> {
			choiseFile.getItems().clear();
			choiseFile.getItems().add("--- Selecciona ---");
			for (File file : csvFiles) {
				choiseFile.getItems().add(file.getName());
			}
			choiseFile.getSelectionModel().selectFirst();

			choiseFile.setOnAction(e -> {
			    String selectedFile = choiseFile.getSelectionModel().getSelectedItem();
			    if (selectedFile != null && !"--- Selecciona ---".equals(selectedFile)) {
			        Path selectedPath = folderPath.resolve(selectedFile);
			        loadFile(selectedPath.toString());
			    }
			});
		});
	}

	private void loadFile(String pathString) {
		dataLogger.logInfo("Leyendo archivo -> " + pathString);
		Path path = Paths.get(pathString);

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

}
