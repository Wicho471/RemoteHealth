package org.axolotlj.remotehealth.core.logger.api;

import static org.axolotlj.remotehealth.core.logger.format.FormaterLoggerUtils.formatLogLine;
import static org.axolotlj.remotehealth.core.logger.format.AnsiConsoleColor.colorForLevel;

import org.axolotlj.remotehealth.core.logger.LogLevel;;

/**
 * Implementación vacía de DataLogger que no realiza ninguna acción.
 */
public class NoOpDataLogger extends DataLogger {

	public NoOpDataLogger() {
		super(null);
	}

	@Override
	public void logInfo(String message) {
		LogLevel level = LogLevel.INFO;
		System.out.println(colorForLevel(level, formatLogLine(level, message)));
	}

	@Override
	public void logWarn(String message) {
		LogLevel level = LogLevel.WARN;
		System.out.println(colorForLevel(level, formatLogLine(level, message)));
	}

	@Override
	public void logException(String message, Exception exception) {
		LogLevel level = LogLevel.ERROR;
		System.out.println(colorForLevel(level, formatLogLine(level, message)));
		exception.printStackTrace();
	}

	@Override
	public void logDebug(String message) {
		LogLevel level = LogLevel.DEBUG;
		System.out.println(colorForLevel(level, formatLogLine(level, message)));
	}

	@Override
	public void close() {
		LogLevel level = LogLevel.WARN;
		System.out.println(colorForLevel(level, formatLogLine(level, "Nada que cerrar")));
	}

	@Override
	public String getLogFilePath() {
		return "";
	}

	@Override
	public void logFatal(String message) {
		LogLevel level = LogLevel.FATAL;
		System.out.println(colorForLevel(level, formatLogLine(level, message)));
	}

	@Override
	public void logException(String message, Throwable throwable) {
		LogLevel level = LogLevel.ERROR;
		System.out.println(colorForLevel(level, formatLogLine(level, message)));
		throwable.printStackTrace();
	}

}
