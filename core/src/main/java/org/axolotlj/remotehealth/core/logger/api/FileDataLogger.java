package org.axolotlj.remotehealth.core.logger.api;

import static org.axolotlj.remotehealth.core.logger.format.AnsiConsoleColor.colorForLevel;
import static org.axolotlj.remotehealth.core.logger.format.FormaterLoggerUtils.formatLogLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.logger.LogCompressor;
import org.axolotlj.remotehealth.core.logger.LogLevel;
import org.axolotlj.remotehealth.core.logger.format.ExceptionReporter;

/**
 * Implementación de DataLogger que escribe logs en archivos locales de forma
 * inmediata.
 */
public class FileDataLogger extends DataLogger {

	private final Object lock = new Object();

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

	public FileDataLogger(Path logDir, File logFile, BufferedWriter writer) {
		super(null);
		if (logFile == null || writer == null) {
			throw new IllegalArgumentException("logFile y writer no pueden ser null");
		}

		this.logFile = logFile;
		this.writer = writer;
	}

	@Override
	public void logInfo(String message) {
		writeLog(LogLevel.INFO, message);
	}

	@Override
	public void logWarn(String message) {
		writeLog(LogLevel.WARN, message);
	}

	@Override
	public void logException(String context, Exception exception) {
		String report = ExceptionReporter.generateReport(context, exception);
		writeLog(LogLevel.ERROR, report);
	}

	@Override
	public void logException(String message, Throwable throwable) {
		String report = ExceptionReporter.generateReport(message, throwable);
		writeLog(LogLevel.ERROR, report);
	}

	@Override
	public void logFatal(String message) {
		writeLog(LogLevel.FATAL, message);
	}

	@Override
	public void logDebug(String message) {
		writeLog(LogLevel.DEBUG, message);
	}

	private void writeLog(LogLevel level, String message) {
		String formatted = formatLogLine(level, message);
		System.out.println(colorForLevel(level, formatted));
		synchronized (lock) {
			try {
				writer.write(formatted);
				writer.newLine();
				writer.flush();
			} catch (IOException e) {
				System.err.println("Error al escribir log en archivo: " + e.getMessage());
			}
		}
	}

	@Override
	public void close() {
		synchronized (lock) {
			try {
				writer.close();
				LogCompressor.overwriteLatest(logFile);
				LogCompressor.compress(logFile);
				LogCompressor.deleteOriginal(logFile);
			} catch (IOException e) {
				System.err.println("Error al cerrar FileDataLogger: " + e.getMessage());
			}
		}
	}

	@Override
	public String getLogFilePath() {
		return logFile.getAbsolutePath();
	}
}
