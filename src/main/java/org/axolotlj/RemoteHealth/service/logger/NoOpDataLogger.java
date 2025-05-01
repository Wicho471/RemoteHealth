package org.axolotlj.RemoteHealth.service.logger;

/**
 * Implementación vacía de DataLogger que no realiza ninguna acción.
 */
public class NoOpDataLogger extends DataLogger {

    public NoOpDataLogger() {
        super(null);
    }

    @Override
    public void logInfo(String message) {}

    @Override
    public void logWarn(String message) {}

    @Override
    public void logError(String message) {}

    @Override
    public void logDebug(String message) {}

    @Override
    public void close() {}

    @Override
    public String getLogFilePath() {
        return "";
    }
}
