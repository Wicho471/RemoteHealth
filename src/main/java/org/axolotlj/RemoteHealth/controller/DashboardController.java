package org.axolotlj.RemoteHealth.controller;

import java.util.Optional;
import java.util.concurrent.*;

import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.app.ui.ChartUtils;
import org.axolotlj.RemoteHealth.app.ui.FxmlUtils;
import org.axolotlj.RemoteHealth.app.ui.ImageViewUtils;
import org.axolotlj.RemoteHealth.app.ui.SeriesUtils;
import org.axolotlj.RemoteHealth.app.ui.TextFieldUtils;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.filters.RealTimeFilters;
import org.axolotlj.RemoteHealth.model.SensorValue;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.service.DataProcessor;
import org.axolotlj.RemoteHealth.service.SystemMonitor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controlador para la vista del panel de datos en tiempo real.
 */
public class DashboardController implements ContextAware {
	private boolean isRecoding = false;

	// ───────────────────── Constantes ─────────────────────
	private static final int MAX_POINTS_ECG = 2500;
	private static final int MAX_POINTS_PLETH = MAX_POINTS_ECG + 1000;

	// ───────────────────── Campos lógicos ─────────────────────
	private final LinkedBlockingQueue<StructureData> updateQueue = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<StructureData> processedQueue;
	private SystemMonitor monitor;

	private ExecutorService parallelExecutor;
	private ScheduledExecutorService scheduler;
	private AppContext appContext;

	private RealTimeFilters butterworthFilterRealTime;
	private int processedSamples = 0;
	private long lastSampleUpdate = System.currentTimeMillis();
	private long initalTime;

	// ───────────────────── Series de gráfica ─────────────────────
	private final XYChart.Series<Number, Number> ecgSeries = new XYChart.Series<>();
	private final XYChart.Series<Number, Number> plethSeries = new XYChart.Series<>();
	private final XYChart.Series<Number, Number> redSeries = new XYChart.Series<>();
	private int currentIndexEgc = 0;
	private int currentIndexPleth = 0;

	// ───────────────────── Elementos FXML ─────────────────────
	@FXML
	private LineChart<Number, Number> ECG;
	@FXML
	private LineChart<Number, Number> PLETH;
	@FXML
	private TextArea BPM, SPO2, TEMP1, TEMP2, MOV;
	@FXML
	private TextField LATENCY, SAMPLES, cpuProcess, cpuSystem, totalMemory, usedMemory, threads, cpuTime, dataRemaining;
	@FXML
	private Button btnClose;
	@FXML
	private TextField pacientNameField;
	@FXML
	private ImageView imgRecordStatus;

	// ───────────────────── Inicialización ─────────────────────

	@FXML
	public void initialize() {
		butterworthFilterRealTime = new RealTimeFilters(300);
		initalTime = System.currentTimeMillis();
		parallelExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		initSystemMonitor();
		setupCharts();
		startDataUpdater();
	}

	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
		this.processedQueue = context.getProcessedQueue();
	}

	// ───────────────────── Manejo de UI ─────────────────────

	@FXML
	private void handleRec() {
		DataProcessor dataProcessor = appContext.getDataProcessor();

		if (isRecoding) {
			boolean stopped = dataProcessor.stopRecordingData();
			if (!stopped) {
				AlertUtil.showErrorAlert("Error", "No se pudo detener la grabación",
						"Verifique el estado del sistema.");
				return;
			}

			isRecoding = false;
			ImageViewUtils.setImage(imgRecordStatus, "/org/axolotlj/RemoteHealth/img/icons/rec-button.png");
			return;
		}

		String patientName = pacientNameField.getText().isBlank() ? "Unknown" : pacientNameField.getText();
		boolean started = dataProcessor.recordData(appContext.getWsManager().getConnectionData(), patientName);
		if (!started) {
			AlertUtil.showErrorAlert("Error", "No se pudo iniciar la grabación",
					"Ya hay una grabación activa o ocurrió un problema.");
			return;
		}

		isRecoding = true;
		ImageViewUtils.setImage(imgRecordStatus, "/org/axolotlj/RemoteHealth/img/icons/stop-record.png");
	}

	@FXML
	private void handleClose() {
		Optional<ButtonType> result = AlertUtil.showConfirmationAlert("Confirma desconexion", null,
				"¿Estas seguro de cerrar la conexion?");

		if (result.isPresent() && result.get() == ButtonType.OK) {
			appContext.getWsManager().disconnect();
			appContext.getDataProcessor().stop();
			appContext.getWsManager().disconnect();
			if (parallelExecutor != null) {
				parallelExecutor.shutdownNow();
				parallelExecutor = null;
			}
			if (scheduler != null) {
				scheduler.shutdownNow();
				scheduler = null;
			}
			if (monitor != null) {
				monitor.stop();
				monitor = null;
			}
			appContext.getSceneManager().switchTo(SceneType.DEVICE_SELECTOR);
		}
	}

	@FXML
	private void configEsp32Handle() {
		System.out.println("Precionado config esp32");
		try {
			BorderPane page = (BorderPane) FxmlUtils.loadFXML("/org/axolotlj/RemoteHealth/fxml/ESP32ToolsScene.fxml").load();

			Stage popupStage = new Stage();
			Scene scene = new Scene(page);
			popupStage.setScene(scene);
			popupStage.setTitle("Ventana Emergente");

			popupStage.initModality(Modality.APPLICATION_MODAL);

			popupStage.showAndWait();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			AlertUtil.showErrorAlert("Error", "Error al abrir la ventana emergente", e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void setupCharts() {
		ChartUtils.setStyle(ECG, "/org/axolotlj/RemoteHealth/css/DashboardStyle.css");
		ChartUtils.setStyle(PLETH, "/org/axolotlj/RemoteHealth/css/DashboardStyle.css");

		PLETH.getStyleClass().add("PLETH");

		ecgSeries.setName("ECG");
		plethSeries.setName("IR");
		redSeries.setName("Red");

		ECG.setAnimated(false);
		PLETH.setAnimated(false);

		for (int i = 0; i < MAX_POINTS_ECG; i++) {
			ecgSeries.getData().add(new XYChart.Data<>(i, 0));
		}

		for (int i = 0; i < MAX_POINTS_PLETH; i++) {
			plethSeries.getData().add(new XYChart.Data<>(i, 0));
			redSeries.getData().add(new XYChart.Data<>(i, 0));
		}

		ECG.getData().add(ecgSeries);
		PLETH.getData().addAll(plethSeries, redSeries);

		ChartUtils.configureXAxis(ECG, MAX_POINTS_ECG, 0, null, true);
		ChartUtils.configureXAxis(PLETH, MAX_POINTS_PLETH, 0, null, true);
	}

	private void initSystemMonitor() {
		if (monitor != null)
			return;
		monitor = new SystemMonitor(cpuProcess, cpuSystem, totalMemory, usedMemory, threads, cpuTime, initalTime,
				() -> {
					int cpuProc = Integer.parseInt(cpuProcess.getText());
					int cpuSys = Integer.parseInt(cpuSystem.getText());
					TextFieldUtils.updateTextFieldColor(cpuProcess, cpuProc, 0, 100);
					TextFieldUtils.updateTextFieldColor(cpuSystem, cpuSys, 0, 100);
				});
		monitor.start();
	}

	// ───────────────────── Actualización de datos ─────────────────────
	private void startDataUpdater() {
		scheduler = Executors.newSingleThreadScheduledExecutor();

		scheduler.scheduleAtFixedRate(() -> {
			int batchSize = 10;
			for (int i = 0; i < batchSize; i++) {
				StructureData data = processedQueue.poll();
				if (data == null)
					break;
				parallelExecutor.submit(() -> processAndEnqueue(data));
			}

			StructureData data;
			while ((data = updateQueue.poll()) != null) {
				StructureData finalData = data;
				Platform.runLater(() -> applyToChart(finalData));
			}
		}, 0, 20, TimeUnit.MILLISECONDS);
	}

	private void processAndEnqueue(StructureData data) {
		updateQueue.offer(data);
	}

	private void applyToChart(StructureData data) {
		if (data.getEcg().isValid()) {
			SeriesUtils.updateSeriesData(ecgSeries, currentIndexEgc,
					butterworthFilterRealTime.filterECG(normECG(data.getEcg().getValue())));
			currentIndexEgc = (currentIndexEgc + 1) % MAX_POINTS_ECG;
		}
		if (data.getIr().isValid() && data.getRed().isValid()) {
			SeriesUtils.updateSeriesData(plethSeries, currentIndexPleth,
					butterworthFilterRealTime.filterIr(normalizePleth(data.getIr().getValue())));
			SeriesUtils.updateSeriesData(redSeries, currentIndexPleth,
					butterworthFilterRealTime.filterRed(normalizePleth(data.getRed().getValue())));
			currentIndexPleth = (currentIndexPleth + 1) % MAX_POINTS_PLETH;
		}

		if (data.getTemp().isValid())
			TEMP1.setText(String.valueOf(data.getTemp().getValue()));
		else if (data.getTemp().getStatus() == SensorValue.Status.ERROR)
			TEMP1.setText("ERR");
		if (data.getAccel().isValid())
			MOV.setText(String.valueOf(data.getAccel().getValue()));
		else if (data.getAccel().getStatus() == SensorValue.Status.ERROR)
			MOV.setText("ERR");

		processedSamples++;
		long now = System.currentTimeMillis();
		if (now - lastSampleUpdate >= 1000) {
			long latency = Math.abs(now - data.getTimeStamp());
			int samplesPerSecond = processedSamples;
			int dataleft = processedQueue.size();

			processedSamples = 0;
			lastSampleUpdate = now;

			Platform.runLater(() -> {
				SAMPLES.setText(String.valueOf(samplesPerSecond));
				TextFieldUtils.updateTextFieldColor(SAMPLES, samplesPerSecond, 500, 0);

				dataRemaining.setText(String.valueOf(dataleft));
				TextFieldUtils.updateTextFieldColor(dataRemaining, dataleft, 0, 100);

				LATENCY.setText(String.valueOf(latency));
				TextFieldUtils.updateTextFieldColor(LATENCY, latency, 0, 1000);
			});
		}
	}

	private double normECG(short value) {
		return (value / 4095.0) * 3.3; 
	}

	private double normalizePleth(int rawValue) {
		return (rawValue / 262143.0) * 100;
	}
}
