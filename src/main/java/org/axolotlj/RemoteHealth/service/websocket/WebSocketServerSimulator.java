package org.axolotlj.RemoteHealth.service.websocket;

import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.glassfish.tyrus.server.Server;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Simulador de servidor WebSocket que envía datos de prueba.
 */
public class WebSocketServerSimulator {
	
	public enum GenerationMode {
	    REAL, SYNTHETIC
	}

    private Server server;
    private ScheduledExecutorService executor;
    private volatile boolean isActive = false;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty(isActive);
    private static DataLogger dataLogger;
    private static GenerationMode generationMode = GenerationMode.REAL;

    public WebSocketServerSimulator(DataLogger dataLogger) {
        WebSocketServerSimulator.dataLogger = dataLogger;
    }

    public void start() {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        rootLogger.setLevel(Level.SEVERE);

        try {
            server = new Server("localhost", 8081, "", null, SimulatedEndpoint.class);
            server.start();
            dataLogger.logInfo("Servidor WebSocket iniciado en ws://localhost:8081/simulator");
            this.isActive = true;
            this.activeProperty.set(true);
        } catch (Exception e) {
            dataLogger.logError("No se pudo iniciar el simulador: " + e.getMessage());
        }
    }

    public void stop(){
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        if (server != null) {
            server.stop();
            server = null;
        }
        this.isActive = false;
        this.activeProperty.set(false);
        dataLogger.logInfo("WebSocket Server detenido.");
    }

    /**
     * Reinicia el servidor WebSocket si está activo. Si no está activo, lo inicia normalmente.
     */
    public void restart() {
        if (isActive) {
            try {
                dataLogger.logInfo("Reiniciando servidor WebSocket...");
                stop();
                // Breve espera para asegurar liberación de recursos
                Thread.sleep(1000);
            } catch (Exception e) {
                dataLogger.logError("Error al detener el servidor durante reinicio: " + e.getMessage());
                return;
            }
            start();
        }
    }
    
    public boolean isActive() {
        return this.isActive;
    }

    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    public static DataLogger getDataLogger() {
        return dataLogger;
    }
    
    public void setGenerationMode(GenerationMode generationMode) {
		WebSocketServerSimulator.generationMode = generationMode;
	}
    
    public static GenerationMode getGenerationMode() {
        return generationMode;
    }

}
