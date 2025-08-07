package org.axolotlj.remotehealth.core.service.websocket;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.simulation.GenerationMode;
import org.glassfish.tyrus.server.Server;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Simulador de servidor WebSocket que envía datos de prueba.
 */
public class WebSocketServerSimulator {
	
	private static final String LOCAL_IPV4 = "127.0.0.1";
	private static final String LOCAL_IPV6 = "::1";
	private static final int PORT = 8081;
	private static final String PATH = "/simulator";
	private static final String NAME = "Simulador";

    private Server server;
    private ScheduledExecutorService executor;
    private volatile boolean isActive = false;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty(isActive);
    private static DataLogger dataLogger = Log.get();
    private static GenerationMode generationMode = GenerationMode.REAL;

    public WebSocketServerSimulator() {
    }

    public void start() {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        rootLogger.setLevel(Level.SEVERE);

        try {
        	server = new Server(LOCAL_IPV4, PORT, "", null, SimulatedEndpoint.class);
            server.start();
            dataLogger.logInfo("Servidor WebSocket iniciado en ws://localhost:8081/simulator");
            this.isActive = true;
            this.activeProperty.set(true);
        } catch (Exception e) {
            dataLogger.logException("No se pudo iniciar el simulador" , e);
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
                dataLogger.logException("Error al detener el servidor durante reinicio" , e);
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
    
    public ConnectionData getConnection() {
    	return new ConnectionData(UUID.randomUUID(), LOCAL_IPV4, LOCAL_IPV6, PATH, PORT, NAME);
	}

}
