package org.axolotlj.remotehealth.core.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.AppContext.ContextAware;
import org.axolotlj.remotehealth.core.AppContext.DisposableController;
import org.axolotlj.remotehealth.core.analysis.bp.core.BPMonitor;
import org.axolotlj.remotehealth.core.analysis.hr.HrMonitor;
import org.axolotlj.remotehealth.core.analysis.spo2.Spo2Monitor;
import org.axolotlj.remotehealth.core.filters.IirRealTimeFilter;
import org.axolotlj.remotehealth.core.sensor.data.DataPoint;

import javafx.fxml.FXML;

/**
 * Controlador abstracto que contiene lógica común de procesamiento de datos en
 * tiempo real.
 */
public abstract class AbstractMonitorController implements ContextAware, DisposableController {

	protected boolean isRecoding = false;
	protected final LinkedBlockingQueue<DataPoint> updateQueue = new LinkedBlockingQueue<>();
	protected LinkedBlockingQueue<DataPoint> processedQueue;
	protected ExecutorService parallelExecutor;
	protected ScheduledExecutorService scheduler;
	protected AppContext appContext;
	protected HrMonitor hrMonitor;
	protected Spo2Monitor spo2Monitor;
	protected BPMonitor bpMonitor;
	protected IirRealTimeFilter realTimeFilter;
	protected int processedSamples = 0;
	protected long lastSampleUpdate = System.currentTimeMillis();
	protected long initialTime;

	@Override
	public void setAppContext(AppContext context) {
		this.appContext = context;
		this.processedQueue = context.getProcessedQueue();
	}

	protected void startDataUpdater() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(() -> {
			int batchSize = 10;
			for (int i = 0; i < batchSize; i++) {
				DataPoint data = processedQueue.poll();
				if (data == null)
					break;
				parallelExecutor.submit(() -> processAndEnqueue(data));
			}

			DataPoint data;
			while ((data = updateQueue.poll()) != null) {
				applyToChart(data);
			}
		}, 0, 20, TimeUnit.MILLISECONDS);
	}

	protected void processAndEnqueue(DataPoint data) {
		updateQueue.offer(data);
	}

	protected abstract void applyToChart(DataPoint data);

	protected void initMonitors(Runnable hrUpdate, Runnable spo2Update, Runnable bpUpdate) {
		hrMonitor = new HrMonitor(250, 5, hr -> hrUpdate.run());
		spo2Monitor = new Spo2Monitor(125.0, 20.0, 110.0, 8.0, spo2 -> spo2Update.run());
		bpMonitor = new BPMonitor(bp -> bpUpdate.run());
	}

	protected double normECG(short value) {
		return ((value / 4095.0) * 3.3) - 1.65;
	}

	protected double normalizePleth(int rawValue) {
		return (rawValue / 262143.0) * 100;
	}
	
	@FXML
	protected abstract void handleClose();
	
	@FXML
	protected abstract void handleRec();

	@Override
	public void dispose() {
		if (scheduler != null) {
			scheduler.shutdownNow();
			scheduler = null;
		}

		if (parallelExecutor != null) {
			parallelExecutor.shutdownNow();
			parallelExecutor = null;
		}

		if (hrMonitor != null) {
			hrMonitor.stop();
			hrMonitor = null;
		}

		if (spo2Monitor != null) {
			spo2Monitor.stop();
			spo2Monitor = null;
		}

		if (bpMonitor != null) {
			bpMonitor.stop();
			bpMonitor = null;
		}

		updateQueue.clear();
		if (processedQueue != null) {
			processedQueue.clear();
		}

		appContext = null;
		processedQueue = null;
		realTimeFilter = null;

		System.gc();
	}
}
