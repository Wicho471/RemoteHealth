package org.axolotlj.remotehealth.desktop.service;

import java.lang.management.ManagementFactory;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;

import com.sun.management.OperatingSystemMXBean;

import javafx.application.Platform;
import javafx.scene.control.TextField;

/**
 * Servicio encargado de monitorear métricas del sistema.
 */
public class SystemMonitor {
	
	private final DataLogger dataLogger = Log.get();

    private final TextField cpuProcess;
    private final TextField cpuSystem;
    private final TextField totalMemory;
    private final TextField usedMemory;
    private final TextField threads;
    private final TextField cpuTime;

    private final long initialTime;

    private final Runnable updateColorCallback;
    private Thread monitorThread;

    public SystemMonitor(
            TextField cpuProcess,
            TextField cpuSystem,
            TextField totalMemory,
            TextField usedMemory,
            TextField threads,
            TextField cpuTime,
            long initialTime,
            Runnable updateColorCallback
    ) {
        this.cpuProcess = cpuProcess;
        this.cpuSystem = cpuSystem;
        this.totalMemory = totalMemory;
        this.usedMemory = usedMemory;
        this.threads = threads;
        this.cpuTime = cpuTime;
        this.initialTime = initialTime;
        this.updateColorCallback = updateColorCallback;
    }

    /**
     * Inicia el hilo de monitoreo.
     */
    @SuppressWarnings("deprecation")
	public void start() {
        monitorThread = new Thread(() -> {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            Runtime runtime = Runtime.getRuntime();
            final int MB = 1024 * 1024;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    double cpuProcessLoad = osBean.getProcessCpuLoad();
                    double cpuSystemLoad = osBean.getSystemCpuLoad();

                    double usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (double) MB;
                    double totalMem = runtime.totalMemory() / (double) MB;

                    int activeThreadCount = Thread.activeCount();
                    long uptimeMillis = System.currentTimeMillis() - initialTime;

                    Platform.runLater(() -> {
                        int cpuProcPercent = (int) Math.ceil(cpuProcessLoad * 100);
                        int cpuSysPercent = (int) Math.ceil(cpuSystemLoad * 100);

                        cpuProcess.setText(cpuProcPercent + "");
                        cpuSystem.setText(cpuSysPercent + "");
                        totalMemory.setText(String.format("%.0f MB", totalMem));
                        usedMemory.setText(String.format("%.0f MB", usedMem));
                        threads.setText(String.valueOf(activeThreadCount));
                        cpuTime.setText(formatDuration(uptimeMillis));

                        updateColorCallback.run(); 
                    });

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                	dataLogger.logInfo("Hilo interrumpido");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    dataLogger.logException("Ocurrio un error inesperado", e);
                }
            }
        }, "SystemMonitorThread");

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    public void stop() {
        if (monitorThread != null && monitorThread.isAlive()) {
            monitorThread.interrupt();
        }
        monitorThread = null;
    }

    
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
}
