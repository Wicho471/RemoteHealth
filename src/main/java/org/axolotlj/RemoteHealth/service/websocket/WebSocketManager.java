package org.axolotlj.RemoteHealth.service.websocket;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.axolotlj.RemoteHealth.model.ConnectionData;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.glassfish.tyrus.client.ClientManager;

import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import javafx.application.Platform;

public class WebSocketManager {

	private BlockingQueue<String> messageQueue;
	private final AtomicReference<Session> sessionRef = new AtomicReference<>();
	private DataLogger dataLogger;
	private ExecutorService wsExecutor;
	private ConnectionData connectionData;
	private volatile boolean isConnected = false;

	public WebSocketManager(BlockingQueue<String> messageQueue, DataLogger dataLogger) {
		this.messageQueue = messageQueue;
		this.dataLogger = dataLogger;
	}

	public void connect(Runnable onSuccess, Runnable onFailure, ConnectionData connectionData, boolean isGlobal) {
		this.connectionData = connectionData;
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		ClientManager client = ClientManager.createClient();
		// Executor con nombre personalizado para evitar hilos anónimos
		wsExecutor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r);
			t.setName("TyrusClientThread-" + connectionData.getUuid().toString().substring(0, 6));
			t.setDaemon(true);
			return t;
		});
		// Configurar Tyrus para usar nuestro ejecutor
		client.getProperties().put("org.glassfish.tyrus.client.threadPool", wsExecutor);

		// Conectar en un hilo controlado
		wsExecutor.submit(() -> {
			try {
				container.connectToServer(new Endpoint() {
					@Override
					public void onOpen(Session session, EndpointConfig config) {
						sessionRef.set(session);
						session.getAsyncRemote().sendText("pong"); // ???
						dataLogger.logInfo("Conexión WebSocket establecida");
						session.addMessageHandler(String.class, message -> processIncomingText(message));
						session.addMessageHandler(ByteBuffer.class, message -> processIncomingBinary(message));
						Platform.runLater(onSuccess);
						isConnected=true;
					}

					@Override
					public void onClose(Session session, CloseReason closeReason) {
						sessionRef.set(null);
						dataLogger.logInfo("Conexión cerrada: " + closeReason.getReasonPhrase());
						isConnected=false;
					}

					@Override
					public void onError(Session session, Throwable thr) {
						thr.printStackTrace();
						dataLogger.logError("Error en WebSocket: " + thr.getMessage());
					}
				}, isGlobal ? connectionData.getUri6() : connectionData.getUri4());

			} catch (Exception e) {
				dataLogger.logError("Error al conectar WebSocket: " + e.getMessage());
				this.connectionData = null;
				isConnected=false;
				Platform.runLater(onFailure);
			}
		});
	}

	private void processIncomingText(String textMessage) {
		if (textMessage.isBlank())
			return;
		try {
			messageQueue.put(textMessage);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			dataLogger.logError("Error al encolar texto: " + e.getMessage());
		}
	}

	private void processIncomingBinary(ByteBuffer byteBuffer) {
		byte[] data = new byte[byteBuffer.remaining()];
		byteBuffer.get(data);
		String base64 = java.util.Base64.getEncoder().encodeToString(data);
		try {
			messageQueue.put(base64);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			dataLogger.logError("Error al encolar binario: " + e.getMessage());
		}
	}

	/**
	 * Envía un mensaje de texto por el WebSocket si la sesión está activa.
	 * 
	 * @param message Mensaje a enviar.
	 * @return true si se envió exitosamente, false en caso contrario.
	 */
	public boolean sendTextMessage(String message) {
		Session session = sessionRef.get();
		if (session != null && session.isOpen()) {
			try {
				session.getAsyncRemote().sendText(message);
				dataLogger.logDebug("Mensaje de texto enviado: " + message);
				return true;
			} catch (Exception e) {
				dataLogger.logError("Error al enviar mensaje: " + e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Envía un mensaje binario por el WebSocket si la sesión está activa.
	 * 
	 * @param data Datos binarios a enviar.
	 * @return true si se envió exitosamente, false en caso contrario.
	 */
	public boolean sendBinaryMessage(byte[] data) {
		Session session = sessionRef.get();
		if (session != null && session.isOpen()) {
			try {
				session.getAsyncRemote().sendBinary(ByteBuffer.wrap(data));
				dataLogger.logDebug("Mensaje binario enviado (" + data.length + " bytes)");
				return true;
			} catch (Exception e) {
				dataLogger.logError("Error al enviar binario: " + e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Cierra la conexión WebSocket si está activa.
	 */
	public void disconnect() {
		try {
			Session session = sessionRef.get();
			if (session != null && session.isOpen()) {
				session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Desconexion controlada"));
				dataLogger.logInfo("WebSocket cerrado correctamente.");
			}
		} catch (Exception e) {
			dataLogger.logError("Error al cerrar WebSocket: " + e.getMessage());
		}
		if (wsExecutor != null && !wsExecutor.isShutdown()) {
			wsExecutor.shutdownNow(); // ⛔ Mata hilos de Tyrus
			wsExecutor = null;
		}
	}
	
	public ConnectionData getConnectionData() {
		return connectionData;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
}
