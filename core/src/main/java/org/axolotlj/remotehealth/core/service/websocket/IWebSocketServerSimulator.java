package org.axolotlj.remotehealth.core.service.websocket;

import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.simulation.GenerationMode;

import javafx.beans.property.BooleanProperty;

/**
 * Contrato para simuladores de servidor WebSocket que envían datos de prueba.
 */
public interface IWebSocketServerSimulator {

	String LOCAL_IPV4 = "127.0.0.1";
	String LOCAL_IPV6 = "::1";
	int PORT=8081;
	String PATH = "/simulator";
	String NAME = "Simulador";

	/**
	 * Inicia el servidor WebSocket.
	 */
	void start();

	/**
	 * Detiene el servidor WebSocket.
	 */
	void stop();

	/**
	 * Reinicia el servidor WebSocket si está activo, o lo inicia si no lo está.
	 */
	void restart();

	/**
	 * Indica si el servidor está activo.
	 * 
	 * @return true si está activo, false en caso contrario
	 */
	boolean isActive();

	/**
	 * Propiedad observable que refleja el estado activo del servidor.
	 * 
	 * @return propiedad booleana activa
	 */
	BooleanProperty activeProperty();

	/**
	 * Configura el modo de generación de datos.
	 * 
	 * @param generationMode modo de generación
	 */
	void setGenerationMode(GenerationMode generationMode);

	/**
	 * Obtiene los datos de conexión del servidor.
	 * 
	 * @return objeto ConnectionData con información de la conexión
	 */
	ConnectionData getConnection();
}
