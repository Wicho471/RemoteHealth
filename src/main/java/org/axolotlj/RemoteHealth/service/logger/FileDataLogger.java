package org.axolotlj.RemoteHealth.service.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.axolotlj.RemoteHealth.config.ConfigFileHelper;

/**
 * Implementación concreta de DataLogger que escribe logs en archivos locales.
 */
public class FileDataLogger extends DataLogger {

    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final File logFile;
    private final BufferedWriter writer;
    private final BlockingQueue<String> writeQueue;
    private final Thread writerThread;
    private volatile boolean running = true;

    public FileDataLogger() throws IOException {
        super(null); 

        Path logDir = ConfigFileHelper.getDLogsDir();
        Files.createDirectories(logDir);

        String fileName = LocalDateTime.now().format(FILE_FORMATTER) + ".log";
        this.logFile = logDir.resolve(fileName).toFile();

        this.writer = new BufferedWriter(new FileWriter(logFile, true));
        this.writeQueue = new LinkedBlockingQueue<>();

        this.writerThread = new Thread(() -> {
            while (running || !writeQueue.isEmpty()) {
                try {
                    String message = writeQueue.take();
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    System.err.println("Error al escribir en archivo: " + e.getMessage());
                }
            }
        }, "DataLogger-WriterThread");

        writerThread.setDaemon(true);
        writerThread.start();
    }

    @Override
    public void logInfo(String message) {
        log("INFO", message);
    }

    @Override
    public void logWarn(String message) {
        log("WARN", message);
    }

    @Override
    public void logError(String message) {
        log("ERROR", message);
    }

    @Override
    public void logDebug(String message) {
        log("DEBUG", message);
    }

    private void log(String level, String message) {
        if (running) {
            String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
            String threadName = Thread.currentThread().getName();
            String formatted = String.format("[%s] [%-5s] [%s] %s", timestamp, level, threadName, message);
            writeQueue.offer(formatted);
        }
    }

    @Override
    public void close() {
        running = false;
        writerThread.interrupt();
        try {
            writerThread.join();
            writer.close();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al cerrar FileDataLogger: " + e.getMessage());
        }
    }

    @Override
    public String getLogFilePath() {
        return logFile.getAbsolutePath();
    }
}
