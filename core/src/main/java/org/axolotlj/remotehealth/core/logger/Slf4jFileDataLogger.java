package org.axolotlj.remotehealth.core.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación de DataLogger que escribe únicamente en consola mediante SLF4J,
 * sin almacenar registros en archivos.
 */
public class Slf4jFileDataLogger extends DataLogger {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jFileDataLogger.class);

    private static final String PREFIX = "Remote Health";

    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

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
        String report = ExceptionReporter.generateReport(context, exception);
        log(LogLevel.ERROR, report);
    }

    @Override
    public void logException(String message, Throwable throwable) {
        String report = ExceptionReporter.generateReport(message, throwable);
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

    private String formatLogLine(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
        String threadName = Thread.currentThread().getName();
        String source = getCallerSource();
        return String.format("[%s] [%s] [%-5s] [%s/%s]: %s",
                PREFIX, timestamp, level.getLabel(), threadName, source, message);
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
        // No hay recursos que cerrar porque no se escriben archivos
    }

    @Override
    public String getLogFilePath() {
        return "No file logging enabled";
    }
}
