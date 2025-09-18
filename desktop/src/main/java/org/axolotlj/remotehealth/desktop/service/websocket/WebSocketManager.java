package org.axolotlj.remotehealth.desktop.service.websocket;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.service.websocket.IWebSocketManager;
import org.glassfish.tyrus.client.ClientManager;

import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

public class WebSocketManager implements IWebSocketManager {
		
	private BlockingQueue<String> messageQueue;
	private final AtomicReference<Session> sessionRef = new AtomicReference<>();
	private DataLogger dataLogger = Log.get();
	private ExecutorService wsExecutor;
	private ConnectionData connectionData;
	private volatile boolean isConnected = false;

	private volatile Consumer<String> onDisconnectHandler;

	public WebSocketManager(BlockingQueue<String> messageQueue) {
		this.messageQueue = messageQueue;
	}

	public void connect(Runnable onSuccess, Consumer<String> onFailure, ConnectionData connectionData, boolean isGlobal) {
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
						session.setMaxIdleTimeout(5000);
						onSuccess.run();
						isConnected = true;
					}

					@Override
					public void onClose(Session session, CloseReason closeReason) {
						sessionRef.set(null);
						dataLogger.logInfo("Conexión cerrada: " + closeReason.getReasonPhrase());
						isConnected = false;
						
					    if (onDisconnectHandler != null 
					            && closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
					        onDisconnectHandler.accept(closeReason.getReasonPhrase());
					    }
					}

					@Override
					public void onError(Session session, Throwable thr) {
						if (thr instanceof Exception exception) {
							dataLogger.logException("Error en WebSocket (sessionId=" + session.getId() + ")",
									exception);
						} else {
							dataLogger.logFatal("Error crítico en WebSocket (sessionId=" + session.getId() + "): "
									+ thr.getClass().getName() + " - " + thr.getMessage());
						}
					}

				}, isGlobal ? connectionData.getUri6() : connectionData.getUri4());

			} catch (ConnectException e) {
				String message = "Conexión rechazada al intentar establecer el WebSocket — Causa: " + e.getMessage();
				dataLogger.logWarn(message);
				this.connectionData = null;
				isConnected = false;
				onFailure.accept(message);

			} catch (SocketTimeoutException e) {
				String message = "Tiempo de espera agotado al intentar conectar con el WebSocket — Causa: " + e.getMessage();
				dataLogger.logWarn(message);
				this.connectionData = null;
				isConnected = false;
				onFailure.accept(message);

			} catch (DeploymentException e) {
				String message = "Fallo al desplegar el cliente WebSocket — Causa: " + e.getMessage();
				dataLogger.logWarn(message);
				this.connectionData = null;
				isConnected = false;
				onFailure.accept(message);

			} catch (Exception e) {
				String message = "Error inesperado al conectar WebSocket: ";
				dataLogger.logException(message, e);
				this.connectionData = null;
				isConnected = false;
				onFailure.accept(message+e.getMessage());
			}
		});
	}

	private void processIncomingText(String textMessage) {
		if (textMessage.isBlank())
			return;
		try {
			enqueMessage(textMessage);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			dataLogger.logException("Error al encolar texto: ", e);
		}
	}

	private void processIncomingBinary(ByteBuffer byteBuffer) {
		byte[] data = new byte[byteBuffer.remaining()];
		byteBuffer.get(data);
		String base64 = java.util.Base64.getEncoder().encodeToString(data);
		try {
			enqueMessage(base64);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			dataLogger.logException("Error al encolar binario: ", e);
		}
	}

	private void enqueMessage(String rawText) throws InterruptedException {
		String messages [] = rawText.split("\n");
		for (String message : messages) {
			messageQueue.put(message);				
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
				dataLogger.logException("Error al enviar mensaje: ", e);
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
				dataLogger.logException("Error al enviar binario: ", e);
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
			dataLogger.logException("Error al cerrar WebSocket", e);
		}
		if (wsExecutor != null && !wsExecutor.isShutdown()) {
			wsExecutor.shutdownNow();
			wsExecutor = null;
		}
	}

	/**
	 * Permite registrar un callback que se ejecuta cuando la conexión se cierra por
	 * causas ajenas a un cierre controlado.
	 */
	public void setOnDisconnectHandler(Consumer<String> handler) {
		this.onDisconnectHandler = handler;
	}

	public ConnectionData getConnectionData() {
		return connectionData;
	}

	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public long getDelay() {
		// TODO Auto-generated method stub
		return 0;
	}
}
