package org.axolotlj.remotehealth.mobile.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.AppContext.ContextAware;
import org.axolotlj.remotehealth.core.AppContext.DisposableController;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.sensor.data.DataPoint;

import com.gluonhq.charm.glisten.mvc.View;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class MonitorContoller implements ContextAware, DisposableController {
	private DataLogger dataLogger = Log.get();
	
	private AppContext appContext;

	protected ExecutorService parallelExecutor;
	protected ScheduledExecutorService scheduler;
	protected LinkedBlockingQueue<DataPoint> processedQueue;

	@FXML
	private View homeView;

	@FXML
	private TextArea test;

	/**
	 * Inicializa los componentes cuando se carga la vista.
	 */
	@FXML
	public void initialize() {
		homeView.setOnShowing(e -> {
			startDataUpdater();
		});
	}

	protected void startDataUpdater() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(() -> {
			DataPoint data = processedQueue.poll();
			dataLogger.logDebug(data.toCsvLine());
			test.appendText(data.toString());
		}, 0, 20, TimeUnit.MILLISECONDS);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
		this.processedQueue = context.getProcessedQueue();
	}

}
