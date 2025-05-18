package org.axolotlj.RemoteHealth.service.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.axolotlj.RemoteHealth.config.ConfigFileHelper;

/**
 * Implementación de DataLogger que escribe logs en archivos locales de forma inmediata.
 */
public class FileDataLogger extends DataLogger {

    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final File logFile;
    private final BufferedWriter writer;

    public FileDataLogger() throws IOException {
        super(null);

        Path logDir = ConfigFileHelper.getDLogsDir();
        Files.createDirectories(logDir);

        String fileName = LocalDateTime.now().format(FILE_FORMATTER) + ".log";
        this.logFile = logDir.resolve(fileName).toFile();

        this.writer = new BufferedWriter(new FileWriter(logFile, true));
    }

    @Override
    public void logInfo(String message) {
        System.out.println("[INFO ] -> " + message);
        writeLog("INFO", message);
    }

    @Override
    public void logWarn(String message) {
        System.err.println("[WARN ] -> " + message);
        writeLog("WARN", message);
    }

    @Override
    public void logError(String message) {
        System.err.println("[ERROR] -> " + message);
        writeLog("ERROR", message);
    }

    @Override
    public void logDebug(String message) {
        System.out.println("[DEBUG] -> " + message);
        writeLog("DEBUG", message);
    }

    private void writeLog(String level, String message) {
        String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
        String threadName = Thread.currentThread().getName();
        String formatted = String.format("[%s] [%-5s] [%s] %s", timestamp, level, threadName, message);

        try {
            writer.write(formatted);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error al escribir log en archivo: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
            LogCompressor.overwriteLatest(logFile);
            LogCompressor.compress(logFile);
            LogCompressor.deleteOriginal(logFile);
        } catch (IOException e) {
            System.err.println("Error al cerrar FileDataLogger: " + e.getMessage());
        }
    }

    @Override
    public String getLogFilePath() {
        return logFile.getAbsolutePath();
    }
}
