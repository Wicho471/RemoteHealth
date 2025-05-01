package org.axolotlj.RemoteHealth.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.*;

import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.Alerts;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.filters.RealTimeFilters;
import org.axolotlj.RemoteHealth.model.SensorValue;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.service.DataProcessor;
import org.axolotlj.RemoteHealth.service.SystemMonitor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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
	            Alerts.showErrorAlert("Error", "No se pudo detener la grabación", "Verifique el estado del sistema.");
	            return;
	        }

	        isRecoding = false;
	        Platform.runLater(() -> imgRecordStatus.setImage(new Image(
	                getClass().getResource("/org/axolotlj/RemoteHealth/img/icons/rec-button.png").toExternalForm()
	        )));
	        return;
	    }

	    String patientName = pacientNameField.getText().isBlank() ? "Unknown" : pacientNameField.getText();
	    boolean started = dataProcessor.recordData(appContext.getWsManager().getConnectionData(), patientName);
	    if (!started) {
	        Alerts.showErrorAlert("Error", "No se pudo iniciar la grabación", "Ya hay una grabación activa o ocurrió un problema.");
	        return;
	    }

	    isRecoding = true;
	    Platform.runLater(() -> imgRecordStatus.setImage(new Image(
	            getClass().getResource("/org/axolotlj/RemoteHealth/img/icons/stop-record.png").toExternalForm()
	    )));
	}



	@FXML
	private void handleClose() {
		Optional<ButtonType> result = Alerts.showConfirmationAlert("Confirma desconexion", null,
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
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/org/axolotlj/RemoteHealth/fxml/ESP32ToolsScene.fxml")); // Cambia a la ruta
																										// correcta
			BorderPane page = (BorderPane) loader.load();

			Stage popupStage = new Stage();
			Scene scene = new Scene(page);
			popupStage.setScene(scene);
			popupStage.setTitle("Ventana Emergente");

			popupStage.initModality(Modality.APPLICATION_MODAL);

			popupStage.showAndWait();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			Alerts.showErrorAlert("Error", "Error al abrir la ventana emergente", e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void setupCharts() {
		ECG.getStylesheets()
				.add(getClass().getResource("/org/axolotlj/RemoteHealth/css/DashboardStyle.css").toExternalForm());
		PLETH.getStylesheets()
				.add(getClass().getResource("/org/axolotlj/RemoteHealth/css/DashboardStyle.css").toExternalForm());
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

		((NumberAxis) ECG.getXAxis()).setAutoRanging(false);
		((NumberAxis) ECG.getXAxis()).setLowerBound(0);
		((NumberAxis) ECG.getXAxis()).setUpperBound(MAX_POINTS_ECG - 1);

		((NumberAxis) PLETH.getXAxis()).setAutoRanging(false);
		((NumberAxis) PLETH.getXAxis()).setLowerBound(0);
		((NumberAxis) PLETH.getXAxis()).setUpperBound(MAX_POINTS_PLETH - 1);
	}

	private void initSystemMonitor() {
		if (monitor != null)
			return;
		monitor = new SystemMonitor(cpuProcess, cpuSystem, totalMemory, usedMemory, threads, cpuTime, initalTime,
				() -> {
					int cpuProc = Integer.parseInt(cpuProcess.getText());
					int cpuSys = Integer.parseInt(cpuSystem.getText());
					updateTextFieldColor(cpuProcess, cpuProc, 0, 100);
					updateTextFieldColor(cpuSystem, cpuSys, 0, 100);
				});
		monitor.start();
	}

	private void updateTextFieldColor(TextField textField, double value, double min, double max) {
		double normalized = Math.max(0, Math.min(1, (value - min) / (max - min)));
		int r, g;

		if (normalized < 0.5) {
			r = (int) (normalized * 2 * 255);
			g = 255;
		} else {
			r = 255;
			g = (int) ((1 - normalized) * 2 * 255);
		}

		String color = String.format("#%02x%02x00", r, g);
		textField.setStyle("-fx-background-color: black;" + "-fx-text-fill: " + color + ";" + "-fx-font-weight: bold;"
				+ "-fx-border-color: black;" + "-fx-border-width: 1;" + "-fx-opacity: 1;");
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
			updateSeriesData(ecgSeries, currentIndexEgc,
					butterworthFilterRealTime.filterECG(normECG(data.getEcg().getValue())));
			currentIndexEgc = (currentIndexEgc + 1) % MAX_POINTS_ECG;
		}
		if (data.getIr().isValid() && data.getRed().isValid()) {
			updateSeriesData(plethSeries, currentIndexPleth,
					butterworthFilterRealTime.filterIr(normalizePleth(data.getIr().getValue())));
			updateSeriesData(redSeries, currentIndexPleth,
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
				updateTextFieldColor(SAMPLES, samplesPerSecond, 500, 0);

				dataRemaining.setText(String.valueOf(dataleft));
				updateTextFieldColor(dataRemaining, dataleft, 0, 100);

				LATENCY.setText(String.valueOf(latency));
				updateTextFieldColor(LATENCY, latency, 0, 1000);
			});
		}
	}

	private void updateSeriesData(XYChart.Series<Number, Number> series, int index, double value) {
		series.getData().get(index).setYValue(value);
	}

	private double normECG(short value) {
		return (value / 4095.0) * 3.3; // <-- usar 4095.0 (un double) para forzar la división real
	}

	private double normalizePleth(int rawValue) {
		return (rawValue / 262143.0) * 100;
	}
}
