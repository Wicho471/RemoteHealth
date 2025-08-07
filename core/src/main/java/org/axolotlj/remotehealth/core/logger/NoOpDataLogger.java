package org.axolotlj.remotehealth.core.logger;

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
    public void logException(String message, Exception exception) {}

    @Override
    public void logDebug(String message) {}

    @Override
    public void close() {}

    @Override
    public String getLogFilePath() {
        return "";
    }

	@Override
	public void logFatal(String message) {}

	@Override
	public void logException(String message, Throwable throwable) {}

}
