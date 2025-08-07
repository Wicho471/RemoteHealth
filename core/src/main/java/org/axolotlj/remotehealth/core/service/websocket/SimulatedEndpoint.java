package org.axolotlj.remotehealth.core.service.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.path.SharedPaths;
import org.axolotlj.remotehealth.core.simulation.DataPayloadGenerator;
import org.axolotlj.remotehealth.core.simulation.RealDataSimulator;
import org.axolotlj.remotehealth.core.simulation.SyntheticDataGenerator;

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
		case REAL -> this.generator = new RealDataSimulator(SharedPaths.REF_CSV.substring(1));
		case SYNTHETIC -> this.generator = new SyntheticDataGenerator(); 
		}
	}

	@OnOpen
	public void onOpen(Session session) {
		executor = Executors.newSingleThreadScheduledExecutor(r -> {
		    Thread thread = new Thread(r);
		    thread.setName("SimulatedEndpointExecutor");
		    thread.setDaemon(true);
		    return thread;
		});

		executor.scheduleAtFixedRate(() -> {
			try {
				String message = generator.generatePayload();
				session.getBasicRemote().sendText(message);
			} catch (IOException e) {
				dataLogger.logException("Error al enviar mensaje desde CSV", e);
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
					dataLogger.logWarn("Executor no se cerró correctamente para cliente: " + session.getId());
				}
			} catch (InterruptedException e) {
				dataLogger.logException("Error al cerrar executor", e);
			}
		}
	}
}
