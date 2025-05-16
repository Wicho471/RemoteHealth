package org.axolotlj.RemoteHealth.service.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.simulation.DataPayloadGenerator;
import org.axolotlj.RemoteHealth.simulation.RealDataSimulator;
import org.axolotlj.RemoteHealth.simulation.SyntheticDataGenerator;
import org.axolotlj.RemoteHealth.util.Paths;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Punto final WebSocket que envía datos simulados periódicamente.
 */
@ServerEndpoint("/simulator")
public class SimulatedEndpoint {

	private ScheduledExecutorService executor;
	private DataPayloadGenerator generator;
	private DataLogger dataLogger;

	public SimulatedEndpoint() {
		this.dataLogger = WebSocketServerSimulator.getDataLogger();
		switch (WebSocketServerSimulator.getGenerationMode()) {
		case REAL -> this.generator = new RealDataSimulator(Paths.REF_CSV.substring(1));
		case SYNTHETIC -> this.generator = new SyntheticDataGenerator(); 
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> {
			try {
				String message = generator.generatePayload();
				session.getBasicRemote().sendText(message);
			} catch (IOException e) {
				dataLogger.logError("Error al enviar mensaje desde CSV: " + e.getMessage());
			}
		}, 5000, 4, TimeUnit.MILLISECONDS);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		dataLogger.logInfo("Mensaje recibido del cliente: " + message);
	}

	@OnClose
	public void onClose(Session session) {
		if (executor != null && !executor.isShutdown()) {
			executor.shutdownNow();
			try {
				if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
					dataLogger.logError("Executor no se cerró correctamente para cliente: " + session.getId());
				}
			} catch (InterruptedException e) {
				dataLogger.logError("Error al cerrar executor: " + e.getMessage());
			}
		}
	}
}
