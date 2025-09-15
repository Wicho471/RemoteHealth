package org.axolotlj.remotehealth.desktop.service.websocket;

import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.service.websocket.IWebSocketServerSimulator;
import org.axolotlj.remotehealth.core.simulation.GenerationMode;
import org.glassfish.tyrus.server.Server;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Simulador de servidor WebSocket que envía datos de prueba.
 */
public class WebSocketServerSimulator implements IWebSocketServerSimulator {

	private Server server;
	private volatile boolean isActive = false;
	private final BooleanProperty activeProperty = new SimpleBooleanProperty(isActive);
	private DataLogger dataLogger = Log.get();
	public static GenerationMode generationMode = GenerationMode.REAL;

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
			dataLogger.logInfo("Servidor WebSocket iniciado en ws://"+LOCAL_IPV4+":"+PORT+PATH);
			this.isActive = true;
			this.activeProperty.set(true);
		} catch (Exception e) {
			dataLogger.logException("No se pudo iniciar el simulador", e);
		}
	}

	public void stop() {
		if (server != null) {
			server.stop();
			server = null;
		}
		this.isActive = false;
		this.activeProperty.set(false);
		dataLogger.logInfo("WebSocket Server detenido.");
	}

	/**
	 * Reinicia el servidor WebSocket si está activo. Si no está activo, lo inicia
	 * normalmente.
	 */
	public void restart() {
		if (isActive) {
			try {
				dataLogger.logInfo("Reiniciando servidor WebSocket...");
				stop();
				Thread.sleep(500);
			} catch (Exception e) {
				dataLogger.logException("Error al detener el servidor durante reinicio", e);
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

	public void setGenerationMode(GenerationMode generationMode) {
		WebSocketServerSimulator.generationMode = generationMode;
	}

	public ConnectionData getConnection() {
		return new ConnectionData(UUID.randomUUID(), LOCAL_IPV4, LOCAL_IPV6, PATH, PORT, NAME);
	}

}
