package org.axolotlj.RemoteHealth.controller;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.axolotlj.RemoteHealth.analysis.RealtimeCardioProcessor;
import org.axolotlj.RemoteHealth.app.Images;
import org.axolotlj.RemoteHealth.app.SceneManager.SceneType;
import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.app.ui.ChartUtils;
import org.axolotlj.RemoteHealth.app.ui.FxmlUtils;
import org.axolotlj.RemoteHealth.app.ui.ImageViewUtils;
import org.axolotlj.RemoteHealth.app.ui.SeriesUtils;
import org.axolotlj.RemoteHealth.app.ui.TextUtils;
import org.axolotlj.RemoteHealth.core.AppContext;
import org.axolotlj.RemoteHealth.core.AppContext.ContextAware;
import org.axolotlj.RemoteHealth.core.AppContext.DisposableController;
import org.axolotlj.RemoteHealth.filters.RealTimeFilters;
import org.axolotlj.RemoteHealth.model.SensorValue;
import org.axolotlj.RemoteHealth.model.SensorValue.Status;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.service.DataProcessor;
import org.axolotlj.RemoteHealth.service.SystemMonitor;
import org.axolotlj.RemoteHealth.util.Paths;


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
public class DashboardController implements ContextAware, DisposableController {
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
	private RealtimeCardioProcessor cardio;

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
	private TextArea BPM, SPO2, TEMP1, MOV;
	@FXML
	private TextField LATENCY, SAMPLES, cpuProcess, cpuSystem, totalMemory, usedMemory, threads, cpuTime, dataRemaining;
	@FXML
	private Button btnClose;
	@FXML
	private TextField pacientNameField;
	@FXML
	private ImageView imgRecordStatus, statusBpm, statusSpo2, statusTemp, statusMov;

	// ───────────────────── Inicialización ─────────────────────

	@FXML
	public void initialize() {
		cardio = new RealtimeCardioProcessor(new RealtimeCardioProcessor.Config(275.0, // sampleRate Hz (ajústalo)
				5, // latidos para la mediana
				6.0, // ventana SpO₂ en segundos
				110.0, 25.0)); // coef A, B

		butterworthFilterRealTime = new RealTimeFilters(275);
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
		if (pacientNameField.getText().isBlank()) {
			AlertUtil.showWarningAlert("Requerimientos incompletos",
					"Para grabar debes de poner el nombre del paciente", "Revisa el campo nombre del paciente");
			pacientNameField.requestFocus();
			return;
		}
		String patientName = pacientNameField.getText().isBlank() ? "Unknown" : pacientNameField.getText();

		DataProcessor dataProcessor = appContext.getDataProcessor();

		if (isRecoding) {
			boolean stopped = dataProcessor.stopRecordingData();
			if (!stopped) {
				AlertUtil.showErrorAlert("Error", "No se pudo detener la grabación",
						"Verifique el estado del sistema.");
				return;
			}

			isRecoding = false;
			ImageViewUtils.setImage(imgRecordStatus, Images.IMG_ICONS_REC_BUTTON);
			return;
		}

		boolean started = dataProcessor.recordData(appContext.getWsManager().getConnectionData(), patientName);
		if (!started) {
			AlertUtil.showErrorAlert("Error", "No se pudo iniciar la grabación",
					"Ya hay una grabación activa o ocurrió un problema.");
			return;
		}

		isRecoding = true;
		ImageViewUtils.setImage(imgRecordStatus, Images.IMG_ICONS_STOP_RECORD);
	}

	@FXML
	private void handleClose() {
		Optional<ButtonType> result = AlertUtil.showConfirmationAlert("Confirma desconexion", null,
				"¿Estas seguro de cerrar la conexion?");

		if (result.isPresent() && result.get() == ButtonType.OK) {
			// Detener y limpiar el monitor
			if (monitor != null) {
				monitor.stop();
				monitor = null;
			}

			// Detener el scheduler
			if (scheduler != null) {
				scheduler.shutdownNow();
				try {
					scheduler.awaitTermination(3, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				scheduler = null;
			}

			// Detener el executor paralelo
			if (parallelExecutor != null) {
				parallelExecutor.shutdownNow();
				try {
					parallelExecutor.awaitTermination(3, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				parallelExecutor = null;
			}

			updateQueue.clear();

			System.out.println("DashboardController cleaned up.");

			appContext.getMessageQueue().clear();
			appContext.getDataProcessor().stop();
			appContext.getWsManager().disconnect();

			appContext.getSceneManager().switchTo(SceneType.DEVICE_SELECTOR);
		}
	}

	@FXML
	private void configEsp32Handle() {
		System.out.println("Precionado config esp32");
		try {
			BorderPane page = (BorderPane) FxmlUtils.loadFXML(Paths.VIEW_SCENE_ESP32TOOLSSCENE_FXML).load();

			Stage popupStage = new Stage();
			Scene scene = new Scene(page);
			popupStage.setScene(scene);
			popupStage.setTitle("Configuracion");

			popupStage.initModality(Modality.APPLICATION_MODAL);

			popupStage.showAndWait();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			AlertUtil.showErrorAlert("Error", "Error al abrir la ventana emergente", e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void setupCharts() {
		ChartUtils.setStyle(ECG, Paths.CSS_DASHBOARDSTYLE_CSS);
		ChartUtils.setStyle(PLETH, Paths.CSS_DASHBOARDSTYLE_CSS);

		PLETH.getStyleClass().add("PLETH");

		ecgSeries.setName("ECG");
		plethSeries.setName("IR");
		redSeries.setName("Red");

		ECG.setAnimated(false);
		PLETH.setAnimated(false);

		SeriesUtils.initializeSeries(ecgSeries, MAX_POINTS_ECG);
		SeriesUtils.initializeSeries(plethSeries, MAX_POINTS_PLETH);
		SeriesUtils.initializeSeries(redSeries, MAX_POINTS_PLETH);

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
					TextUtils.updateTextFieldColor(cpuProcess, cpuProc, 0, 100);
					TextUtils.updateTextFieldColor(cpuSystem, cpuSys, 0, 100);
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
			double filtered = butterworthFilterRealTime.filterECG(normECG(data.getEcg().getValue()));
			SeriesUtils.updateSeriesData(ecgSeries, currentIndexEgc, filtered);
			currentIndexEgc = (currentIndexEgc + 1) % MAX_POINTS_ECG;
			cardio.addEcgSample(filtered, data.getTimeStamp()).ifPresent(bpm -> {
				int intBpm = (int) bpm;
				TextUtils.setText(BPM, intBpm);
				if (intBpm > 100 || intBpm < 60) {
					ImageViewUtils.setImage(statusBpm, Images.IMG_VITALS_HEARTH_ALERT);
				} else {
					ImageViewUtils.setImage(statusBpm, Images.IMG_VITALS_OK);
				}
			});

		} else if (data.getEcg().getStatus() == Status.ERROR){
			ImageViewUtils.setImage(statusBpm, Images.IMG_VITALS_ASK);
		}
		
		
		if (data.getIr().isValid() && data.getRed().isValid()) {
			double irFiltered = butterworthFilterRealTime.filterIr(normalizePleth(data.getIr().getValue()));
			double redFiltered = butterworthFilterRealTime.filterRed(normalizePleth(data.getRed().getValue()));

			SeriesUtils.updateSeriesData(plethSeries, currentIndexPleth, irFiltered);
			SeriesUtils.updateSeriesData(redSeries, currentIndexPleth, redFiltered);
			currentIndexPleth = (currentIndexPleth + 1) % MAX_POINTS_PLETH;

			if (irFiltered < 10.0 || redFiltered < 10.0) {
				TextUtils.setText(SPO2, "???");
				ImageViewUtils.setImage(statusSpo2, Images.IMG_VITALS_ASK);
			} else {
				cardio.addPlethSample(irFiltered, redFiltered, data.getTimeStamp()).ifPresent(spo2 -> {
					int intSpo2 = (int) spo2;
					TextUtils.setText(SPO2, intSpo2);
					if (intSpo2 < 90) {
						ImageViewUtils.setImage(statusSpo2, Images.IMG_VITALS_DYSPNOEA_ALERT);
					} else {
						ImageViewUtils.setImage(statusSpo2, Images.IMG_VITALS_OK);
					}
				});
			}
		}

		if (data.getTemp().isValid()) {
			float currentTemp = data.getTemp().getValue();
			TextUtils.setText(TEMP1, currentTemp + "°C");
			if (currentTemp < 36.1f || currentTemp > 37.8) {
				ImageViewUtils.setImage(statusTemp, Images.IMG_VITALS_TEMP_ALERT);
			} else {
				ImageViewUtils.setImage(statusTemp, Images.IMG_VITALS_OK);
			}
		} else if (data.getTemp().getStatus() == SensorValue.Status.ERROR) {
			TextUtils.setText(TEMP1, "ERR");
			ImageViewUtils.setImage(statusTemp, Images.IMG_VITALS_ASK);
		}
		
		if (data.getAccel().isValid()) {
			float mov = data.getAccel().getValue();
			TextUtils.setText(MOV, mov + "g");
			if(mov < 0.95f || mov > 1.05) {
				ImageViewUtils.setImage(statusMov, Images.IMG_VITALS_STOP_ALERT);
			} else {
				ImageViewUtils.setImage(statusMov, Images.IMG_VITALS_OK);
			}
		}
		else if (data.getAccel().getStatus() == SensorValue.Status.ERROR) {
			TextUtils.setText(MOV, "ERR");
			ImageViewUtils.setImage(statusTemp, Images.IMG_VITALS_ASK);
		}

		processedSamples++;
		long now = System.currentTimeMillis();
		if (now - lastSampleUpdate >= 1000) {
			long latency = Math.abs(now - data.getTimeStamp());
			int samplesPerSecond = processedSamples;
			int dataleft = processedQueue.size();

			processedSamples = 0;
			lastSampleUpdate = now;

			TextUtils.setText(SAMPLES, samplesPerSecond);
			TextUtils.updateTextFieldColor(SAMPLES, samplesPerSecond, 300, 0);

			TextUtils.setText(dataRemaining, dataleft);
			TextUtils.updateTextFieldColor(dataRemaining, dataleft, 0, 100);

			TextUtils.setText(LATENCY, latency);
			TextUtils.updateTextFieldColor(LATENCY, latency, 0, 1000);
		}
	}

	private double normECG(short value) {
		return ((value / 4095.0) * 3.3) - 1.65;
	}

	private double normalizePleth(int rawValue) {
		return (rawValue / 262143.0) * 100;
	}

	@Override
	public void dispose() {
		System.out.println("Disposing DashboardController...");

		// 🚨 Detener el SystemMonitor
		if (monitor != null) {
			monitor.stop();
			monitor = null;
		}

		// 🚨 Detener el scheduler
		if (scheduler != null) {
			scheduler.shutdownNow();
			try {
				scheduler.awaitTermination(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			scheduler = null;
		}

		// 🚨 Detener el executor paralelo
		if (parallelExecutor != null) {
			parallelExecutor.shutdownNow();
			try {
				parallelExecutor.awaitTermination(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			parallelExecutor = null;
		}

		// 🚨 Limpiar la cola de actualización
		updateQueue.clear();

		// 🚨 Limpiar las gráficas
		if (ECG != null) {
			ECG.getData().clear();
		}
		if (PLETH != null) {
			PLETH.getData().clear();
		}

		System.out.println("DashboardController disposed.");
	}

}
