package org.axolotlj.RemoteHealth.service.logger;

import java.nio.file.Path;

public abstract class DataLogger {

    public DataLogger(Path dummy) {}

    public abstract void logInfo(String message);
    public abstract void logWarn(String message);
    public abstract void logError(String message);
    public abstract void logDebug(String message);
    public abstract void close();
    public abstract String getLogFilePath();
}

/**
 * Clase responsable de registrar los mensajes y eventos en un archivo de texto.
 * Soporta múltiples niveles de log y escritura asíncrona para no bloquear el hilo principal.
 */

