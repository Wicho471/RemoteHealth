package org.axolotlj.remotehealth.core.service.websocket;

import java.util.function.Consumer;

import org.axolotlj.remotehealth.core.model.ConnectionData;

/**
 * Contrato para manejar conexiones WebSocket.
 * Define las operaciones básicas de conexión, envío de mensajes y cierre.
 */
public interface IWebSocketManager {

    /**
     * Establece una conexión WebSocket.
     *
     * @param onSuccess Acción a ejecutar cuando la conexión se establece exitosamente.
     * @param onFailure Acción a ejecutar cuando la conexión falla.
     * @param connectionData Datos de conexión (URI, identificadores, etc.).
     * @param isGlobal Define si se debe usar IPv6 (true) o IPv4 (false).
     */
    void connect(Runnable onSuccess, Consumer<String> onFailure, ConnectionData connectionData, boolean isGlobal);

    /**
     * Envía un mensaje de texto por el WebSocket.
     *
     * @param message Mensaje a enviar.
     * @return true si se envió exitosamente, false en caso contrario.
     */
    boolean sendTextMessage(String message);

    /**
     * Envía un mensaje binario por el WebSocket.
     *
     * @param data Datos binarios a enviar.
     * @return true si se envió exitosamente, false en caso contrario.
     */
    boolean sendBinaryMessage(byte[] data);

    /**
     * Cierra la conexión WebSocket si está activa.
     */
    void disconnect();

    /**
     * Obtiene los datos de conexión actuales.
     *
     * @return objeto {@link ConnectionData} asociado a la sesión.
     */
    ConnectionData getConnectionData();

    /**
     * Indica si el WebSocket está actualmente conectado.
     *
     * @return true si la conexión está activa, false en caso contrario.
     */
    boolean isConnected();
    
    
    void setOnDisconnectHandler(Consumer<String> handler);
    
    long getDelay();
}
