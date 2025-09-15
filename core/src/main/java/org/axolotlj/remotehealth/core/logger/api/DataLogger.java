package org.axolotlj.remotehealth.core.logger.api;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public abstract class DataLogger {

	public static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
	
    public DataLogger(Path dummy) {}

    public abstract void logInfo(String message);
    public abstract void logWarn(String message);
    public abstract void logException(String message, Exception exception);
    public abstract void logException(String message, Throwable throwable);
    public abstract void logDebug(String message);
    public abstract void logFatal(String message);
    public abstract void close();
    public abstract String getLogFilePath();
}

