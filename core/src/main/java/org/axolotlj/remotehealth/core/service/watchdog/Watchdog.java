package org.axolotlj.remotehealth.core.service.watchdog;

import java.util.concurrent.atomic.AtomicLong;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;

public class Watchdog {
    private final DataLogger dataLogger = Log.get();

    private final AtomicLong lastResponse = new AtomicLong(System.currentTimeMillis());
    private final long timeoutMs;
    private final TaskToWatch watch;
    private final OnWatchdogTrigger onTrigger;

    private volatile boolean running = true;

    public Watchdog(long timeoutMs, TaskToWatch watch, OnWatchdogTrigger onTrigger) {
        this.timeoutMs = timeoutMs;
        this.watch = watch;
        this.onTrigger = onTrigger;
    }

    public void start() {
        Thread watchdogThread = new Thread(() -> {
            while (running) {
                try {
                    long now = System.currentTimeMillis();
                    long elapsed = now - lastResponse.get();

                    if (elapsed > timeoutMs) {
                        dataLogger.logWarn("[WATCHDOG] Tarea no responde desde hace " + elapsed + "ms");
                        String dump = ThreadDumpGenerator.generateThreadDump();
                        onTrigger.onTriggered(elapsed, dump);
                        
                    }

                    watch.check(() -> lastResponse.set(System.currentTimeMillis()));

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    dataLogger.logException("Hilo interrumpido", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    dataLogger.logException("Error en Watchdog loop", e);
                }
            }
        }, "Watchdog-Thread");

        watchdogThread.setDaemon(true);
        watchdogThread.start();
    }

    public void stop() {
        dataLogger.logInfo("Finalizando el watchdog");
        running = false;
    }
}
