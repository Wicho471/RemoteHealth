package org.axolotlj.remotehealth.core.logger.api;

import static org.axolotlj.remotehealth.core.logger.format.ExceptionReporter.generateReport;
import static org.axolotlj.remotehealth.core.logger.format.FormaterLoggerUtils.formatLogLine;

import org.axolotlj.remotehealth.core.logger.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de DataLogger que escribe únicamente en consola mediante SLF4J,
 * sin almacenar registros en archivos.
 */
public class Slf4jFileDataLogger extends DataLogger {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jFileDataLogger.class);

    public Slf4jFileDataLogger() {
        super(null);
    }

    @Override
    public void logInfo(String message) {
        log(LogLevel.INFO, message);
    }

    @Override
    public void logWarn(String message) {
        log(LogLevel.WARN, message);
    }

    @Override
    public void logException(String context, Exception exception) {
        String report = generateReport(context, exception);
        log(LogLevel.ERROR, report);
    }

    @Override
    public void logException(String message, Throwable throwable) {
        String report = generateReport(message, throwable);
        log(LogLevel.ERROR, report);
    }

    @Override
    public void logFatal(String message) {
        log(LogLevel.FATAL, message);
    }

    @Override
    public void logDebug(String message) {
        log(LogLevel.DEBUG, message);
    }

    private void log(LogLevel level, String message) {
        String formatted = formatLogLine(level, message);
        switch (level) {
            case INFO -> logger.info(formatted);
            case WARN -> logger.warn(formatted);
            case ERROR, FATAL -> logger.error(formatted);
            case DEBUG -> logger.debug(formatted);
            default -> logger.info(formatted);
        }
    }

    @Override
    public void close() {
        // No hay recursos que cerrar porque no se escriben archivos
    }

    @Override
    public String getLogFilePath() {
        return "No file logging enabled";
    }
}
