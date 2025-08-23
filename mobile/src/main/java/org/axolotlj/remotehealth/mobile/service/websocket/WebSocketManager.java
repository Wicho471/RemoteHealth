package org.axolotlj.remotehealth.mobile.service.websocket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.service.websocket.IWebSocketManager;
import org.axolotlj.remotehealth.mobile.network.DualStackDns;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Gestor de conexiones WebSocket basado en OkHttp.
 */
public class WebSocketManager implements IWebSocketManager {

	private BlockingQueue<String> messageQueue;
	private final AtomicReference<WebSocket> socketRef = new AtomicReference<>();
	private DataLogger dataLogger = Log.get();
	private ConnectionData connectionData;
	private volatile boolean isConnected = false;
	private OkHttpClient client;

	public WebSocketManager(BlockingQueue<String> messageQueue) {
		this.messageQueue = messageQueue;
		this.client = new OkHttpClient.Builder()
			    .dns(new DualStackDns(true)) // prefer IPv6, fallback IPv4
			    .connectTimeout(30, TimeUnit.SECONDS)
			    .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket: no limitar lectura
			    .writeTimeout(30, TimeUnit.SECONDS)
			    .pingInterval(60, TimeUnit.SECONDS)
			    .build();


	}

	/**
	 * Conecta al WebSocket usando OkHttp.
	 *
	 * @param onSuccess      Acción a ejecutar en conexión exitosa.
	 * @param onFailure      Acción a ejecutar en fallo de conexión.
	 * @param connectionData Datos de conexión.
	 * @param isGlobal       Indica si debe usarse URI IPv6 o IPv4.
	 */
	public void connect(Runnable onSuccess, Runnable onFailure, ConnectionData connectionData, boolean isGlobal) {
		this.connectionData = connectionData;
		String url = isGlobal ? connectionData.getUri6().toString() : connectionData.getUri4().toString();
		
		dataLogger.logDebug("Direccion de conexion -> '" + url + "'");

		Request request = new Request.Builder().url(url).build();

		client.newWebSocket(request, new WebSocketListener() {
			@Override
			public void onOpen(WebSocket webSocket, Response response) {
				socketRef.set(webSocket);
				dataLogger.logInfo("Conexión WebSocket establecida: " + url);
				isConnected = true;
				onSuccess.run();
			}

			@Override
			public void onMessage(WebSocket webSocket, String text) {
				processIncomingText(text);
			}

			@Override
			public void onMessage(WebSocket webSocket, ByteString bytes) {
				processIncomingBinary(bytes);
			}

			@Override
			public void onClosing(WebSocket webSocket, int code, String reason) {
				webSocket.close(1000, "Cierre controlado");
				dataLogger.logInfo("Conexión WebSocket cerrándose: " + reason);
				isConnected = false;
			}

			@Override
			public void onClosed(WebSocket webSocket, int code, String reason) {
				dataLogger.logInfo("Conexión WebSocket cerrada: " + reason);
				socketRef.set(null);
				isConnected = false;
			}

			@Override
			public void onFailure(WebSocket webSocket, Throwable t, Response response) {
				dataLogger.logException("Fallo en WebSocket: ", t);
				socketRef.set(null);
				isConnected = false;
				
				if(isGlobal) {
					connect(onSuccess, onFailure, connectionData, false);
				} else {
					onFailure.run();
				}
			}
		});
	}

	private void processIncomingText(String textMessage) {
		if (textMessage == null || textMessage.isBlank())
			return;
		try {
			messageQueue.put(textMessage);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			dataLogger.logException("Error al encolar texto: ", e);
		}
	}

	private void processIncomingBinary(ByteString bytes) {
		String base64 = bytes.base64();
		try {
			messageQueue.put(base64);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			dataLogger.logException("Error al encolar binario: ", e);
		}
	}

	public boolean sendTextMessage(String message) {
		WebSocket socket = socketRef.get();
		if (socket != null) {
			boolean sent = socket.send(message);
			if (sent) {
				dataLogger.logDebug("Mensaje enviado: " + message);
				return true;
			}
		}
		return false;
	}

	public boolean sendBinaryMessage(byte[] data) {
		WebSocket socket = socketRef.get();
		if (socket != null) {
			boolean sent = socket.send(ByteString.of(data));
			if (sent) {
				dataLogger.logDebug("Mensaje binario enviado (" + data.length + " bytes)");
				return true;
			}
		}
		return false;
	}

	public void disconnect() {
		WebSocket socket = socketRef.get();
		if (socket != null) {
			socket.close(1000, "Desconexión controlada");
			dataLogger.logInfo("WebSocket cerrado correctamente.");
		}
		isConnected = false;
	}

	public ConnectionData getConnectionData() {
		return connectionData;
	}

	public boolean isConnected() {
		return isConnected;
	}
}
