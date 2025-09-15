package org.axolotlj.remotehealth.desktop.service.websocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.path.SharedPaths;
import org.axolotlj.remotehealth.core.simulation.DataPayloadGenerator;
import org.axolotlj.remotehealth.core.simulation.RealDataSimulator;
import org.axolotlj.remotehealth.core.simulation.SyntheticDataGenerator;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Punto final WebSocket que envía datos simulados periódicamente.
 */
@ServerEndpoint("/simulator")
public class SimulatedEndpoint {

	private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();
	private static final DataLogger dataLogger = Log.get();

	private static DataPayloadGenerator generator;
	private static ScheduledExecutorService scheduler;

	public SimulatedEndpoint() {
		switch (WebSocketServerSimulator.generationMode) {
		case REAL -> SimulatedEndpoint.generator = new RealDataSimulator(SharedPaths.REF_CSV.substring(1));
		case SYNTHETIC -> SimulatedEndpoint.generator = new SyntheticDataGenerator();
		}
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		sessions.add(session);
		dataLogger.logInfo("Nueva conexión -> " + session.getId());

		// URI de conexión
		dataLogger.logInfo("Ruta -> " + session.getRequestURI());
		dataLogger.logInfo("Conexiones activas -> " + sessions.size());

		synchronized (SimulatedEndpoint.class) {
			if (scheduler == null || scheduler.isShutdown()) {
				scheduler = Executors.newSingleThreadScheduledExecutor();
				scheduler.scheduleAtFixedRate(() -> {
					try {
						String payload = generator.generatePayload();
						for (Session s : sessions) {
							if (s.isOpen()) {
								s.getAsyncRemote().sendText(payload, result -> {
									if (!result.isOK()) {
										dataLogger.logWarn("Fallo al enviar a " + s.getId() + ": "
												+ result.getException().getMessage());
									}
								});
							}
						}
					} catch (Exception e) {
						dataLogger.logWarn("Error en envío global: " + e.getMessage());
					}
				}, 1000, 4, TimeUnit.MILLISECONDS);
				dataLogger.logDebug("Scheduler iniciado, sesiones activas: " + sessions.size());
			}
		}
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		dataLogger.logInfo("Mensaje de " + session.getId() + ": " + message);
	}

	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
		dataLogger.logInfo("Conexión cerrada -> " + session.getId());
		dataLogger.logInfo("Conexiones activas -> " + sessions.size());
		synchronized (SimulatedEndpoint.class) {
			if (sessions.isEmpty() && scheduler != null && !scheduler.isShutdown()) {
				scheduler.shutdownNow();
				dataLogger.logDebug("Scheduler detenido, no hay conexiones activas.");
			}
		}
	}
}
