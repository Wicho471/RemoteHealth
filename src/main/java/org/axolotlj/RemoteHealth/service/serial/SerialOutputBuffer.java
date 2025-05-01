package org.axolotlj.RemoteHealth.service.serial;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SerialOutputBuffer {

    private final TextArea outputArea;
    private final BlockingQueue<String> buffer = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledTask;

    private final int flushIntervalMs;

    public SerialOutputBuffer(TextArea outputArea) {
        this(outputArea, 50); // por defecto, cada 50 ms
    }

    public SerialOutputBuffer(TextArea outputArea, int flushIntervalMs) {
        this.outputArea = outputArea;
        this.flushIntervalMs = flushIntervalMs;
    }

    public void appendData(String data) {
        buffer.offer(data);
    }

    public void start() {
        if (scheduledTask != null && !scheduledTask.isDone()) return;

        scheduledTask = executor.scheduleAtFixedRate(() -> {
            if (!buffer.isEmpty()) {
                List<String> drained = new ArrayList<>();
                buffer.drainTo(drained);

                StringBuilder combined = new StringBuilder();
                for (String s : drained) {
                    combined.append(s);
                }

                Platform.runLater(() -> {
                    outputArea.appendText(combined.toString());
                    outputArea.setScrollTop(Double.MAX_VALUE);
                });
            }
        }, 0, flushIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }
        executor.shutdownNow();
    }

    public void clear() {
        buffer.clear();
        Platform.runLater(outputArea::clear);
    }
}
