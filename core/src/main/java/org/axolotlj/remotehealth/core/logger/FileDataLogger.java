package org.axolotlj.remotehealth.core.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de DataLogger que escribe logs en archivos locales de forma
 * inmediata.
 */
public class FileDataLogger extends DataLogger {

	private Logger log;

	private final Object lock = new Object();

	private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
	private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private final File logFile;
	private final BufferedWriter writer;

	public FileDataLogger(Class<?> clazz) throws IOException {
		super(null);
		this.log = LoggerFactory.getLogger(clazz);

		System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
		System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
		System.setProperty("org.slf4j.simpleLogger.showLevel", "false");

		Path logDir = ConfigFileHelper.getDLogsDir();
		Files.createDirectories(logDir);

		String fileName = LocalDateTime.now().format(FILE_FORMATTER) + ".log";
		this.logFile = logDir.resolve(fileName).toFile();

		this.writer = new BufferedWriter(new FileWriter(logFile, true));
	}

	public FileDataLogger(Path logDir, File logFile, BufferedWriter writer, Class<?> clazz) {
		super(null);
		if (logFile == null || writer == null) {
			throw new IllegalArgumentException("logFile y writer no pueden ser null");
		}

		this.log = LoggerFactory.getLogger(clazz);
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
		System.out.println(AnsiConsoleColor.colorForLevel(level, "[Remote Health] " + message));
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

	private String formatLogLine(LogLevel level, String message) {
		String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
		String threadName = Thread.currentThread().getName();
		String source = getCallerSource();
		return String.format("[%s] [%-5s] [%s/%s]: %s", timestamp, level.getLabel(), threadName, source, message);
	}

	private String getCallerSource() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		for (int i = 2; i < stack.length; i++) {
			StackTraceElement element = stack[i];
			String className = element.getClassName();

			if (!className.equals(this.getClass().getName()) && !className.equals(Thread.class.getName())) {

				String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
				String methodName = element.getMethodName();
				return simpleClassName + "." + methodName;
			}
		}
		return "UnknownSource";
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

	public void setLog(Logger log) {
		this.log = log;
	}
}
